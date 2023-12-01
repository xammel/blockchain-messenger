package com.xammel.scalablockchain.actors

import akka.actor.{ActorRef, Props, Status}
import akka.pattern.ask
import com.xammel.scalablockchain.actors.Miner.ReadyYourself
import com.xammel.scalablockchain.crypto.Crypto.encrypt
import com.xammel.scalablockchain.models._
import com.xammel.scalablockchain.pubsub.PubSub._

import java.security.PublicKey
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/*
Plan:
- Blockchain-based messaging service
- To be a participant in messaging, you must be a node hosting the blockchain
- Sending a message costs money, mining a block earns money
- Each node has a private and public key. Messages stored on the blockchain are encrypted with the recipient's public key.
  It can be decrypted with the recipient's private key.
- Voila
 */
class Node(nodeId: String, mediator: ActorRef) extends ScalaBlockchainActor[Node.NodeMessage] {

  import Node._

  private val broker = context.actorOf(Broker.props, Broker.actorName)
  private val miner  = context.actorOf(Miner.props, Miner.actorName)
  private val blockchain =
    context.actorOf(Blockchain.props(EmptyChain, nodeId), Blockchain.actorName)
  private val keeper = context.actorOf(KeeperOfKeys.props(nodeId, mediator), KeeperOfKeys.actorName)

  mediator ! subscribeNewBlock(self)
  mediator ! subscribeTransaction(self)
  mediator ! subscribeGetPublicKey(keeper)

  miner ! ReadyYourself

  override def handleMessages: ReceiveType[NodeMessage] = {
    case TransactionMessage(message, messageNodeId) =>
      log.info(s"Received transaction message from $messageNodeId")
      /*
      plan:
      1. transaction is a reward or a message. If reward just add to broker pending txns
      2. if message...
      3. get public key for the recipient
      4. encrypt message payload with the public key
      5. send the new message, with new payload to the broker to add
       */
      keeper.askAndMap(KeeperOfKeys.GetPublicKey(message)) { recipientPublicKey: PublicKey =>
        val encryptedMessagePayload: String = encrypt(recipientPublicKey)(message.message)
        val encryptedMessage: Message       = message.copy(message = encryptedMessagePayload)
        broker ! Broker.AddTransactionToPending(encryptedMessage)
      }
//      broker ! Broker.AddTransactionToPending(message)
    case AddTransaction(transaction) => mediator ! publishTransaction(TransactionMessage(transaction, nodeId))
    case CheckPowSolution(solution) =>
      val node = sender()
      (blockchain ? Blockchain.GetLastHash).mapTo[String] onComplete {
        case Success(hash: String) => miner.tell(Miner.Validate(hash, solution), node)
        case Failure(e)            => node ! Status.Failure(e)
      }
    case AddBlock(proof, transactions, timestamp) =>
      (self ? CheckPowSolution(proof)) givenSuccess { _ =>
        broker ! Broker.DiffTransaction(transactions)
        blockchain.tell(
          Blockchain.AddBlockCommand(transactions, proof, timestamp),
          sender
        )
        miner ! ReadyYourself
      }

    case Mine =>
      val lastHashFuture: Future[String] = (blockchain ? Blockchain.GetLastHash).mapTo[String]
      lastHashFuture givenSuccess { hash: String =>
        val proofOfWorkFuture: Future[Long] = (miner ? Miner.Mine(hash)).mapTo[Future[Long]].flatten
        proofOfWorkFuture givenSuccess { solution => rewardMiningAndAddBlock(solution) }
      }
    case GetTransactions => broker forward Broker.GetPendingTransactions
    case GetStatus       => blockchain forward Blockchain.GetChain
    case ReadMessages =>
      val node = sender()
      blockchain.askAndMap(Blockchain.GetChain) { chain: Chain =>
        val populatedBlocks: List[PopulatedBlock]   = chain.blocks.collect { case p: PopulatedBlock => p }
        val receivedTransactions: List[Transaction] = populatedBlocks.flatMap(_.transactions).filter(_.beneficiary == nodeId)
        val receivedMessages: List[Message]         = receivedTransactions.collect { case m: Message => m }
        keeper.askAndMap(KeeperOfKeys.ReadMessages(receivedMessages)) { messages => node ! messages }
      }
    //TODO don't think these two are used
    case GetLastBlockIndex => blockchain forward Blockchain.GetLastIndex
    case GetLastBlockHash  => blockchain forward Blockchain.GetLastHash
  }

  private def rewardMiningAndAddBlock(solution: Long): Unit = {
    //TODO should there be a criteria which only allows blocks to be mined if there
    // exist pending transactions. otherwise blocks can be mined with just the 1 mining reward
    // transaction in them

    val time = System.currentTimeMillis()

    broker ! Broker.AddTransactionToPending(createMiningRewardTransaction(nodeId))
    (broker ? Broker.GetPendingTransactions).mapTo[List[Transaction]] onComplete {
      case Success(transactions) => mediator ! publishNewBlock(AddBlock(solution, transactions, time))
      case Failure(exception)    => sender ! Failure(exception)
    }
    miner ! Miner.ReadyYourself
  }
}

object Node extends ActorName {
  sealed trait NodeMessage

  case class AddTransaction(message: Message) extends NodeMessage

  case class TransactionMessage(message: Message, nodeId: String) extends NodeMessage

  case class CheckPowSolution(solution: Long) extends NodeMessage

  case class AddBlock(proof: Long, transactions: List[Transaction], timestamp: Long) extends NodeMessage

  case object GetTransactions extends NodeMessage

  case object Mine extends NodeMessage

  case object GetStatus extends NodeMessage

  //TODO these two aren't used yet...
  case object GetLastBlockIndex extends NodeMessage

  case object GetLastBlockHash extends NodeMessage

  case class GetRecipientPublicKey(recipientNodeId: String) extends NodeMessage
  case object ReadMessages                                  extends NodeMessage

  def props(nodeId: String, mediator: ActorRef): Props = Props(new Node(nodeId, mediator))

  //TODO edit amount to be configurable
  def createMiningRewardTransaction(nodeId: String): Transaction = MiningReward("theBank", nodeId)
}
