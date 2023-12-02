package com.xammel.scalablockchain.actors

import akka.actor.{ActorRef, Props, Status}
import akka.pattern.ask
import com.xammel.scalablockchain.actors.Miner.ReadyYourself
import com.xammel.scalablockchain.models._
import com.xammel.scalablockchain.pubsub.PubSub._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

  private val broker     = context.actorOf(Broker.props, Broker.actorName)
  private val miner      = context.actorOf(Miner.props, Miner.actorName)
  private val blockchain = context.actorOf(Blockchain.props(EmptyChain, nodeId), Blockchain.actorName)
  private val keeper     = context.actorOf(KeeperOfKeys.props(nodeId, mediator), KeeperOfKeys.actorName)

  mediator ! subscribeNewBlock(self)
  mediator ! subscribeTransaction(self)

  miner ! ReadyYourself

  override def handleMessages: ReceiveType[NodeMessage] = {
    case TransactionMessage(message, messageNodeId) =>
      //TODO check the node has enough tokens to send a message
      log.info(s"Received transaction message from $messageNodeId")
      keeper.askAndMap(KeeperOfKeys.EncryptMessage(message)) { encryptedMessage: MessageTransaction =>
        broker ! Broker.AddTransactionToPending(encryptedMessage)
      }
    case AddTransaction(transaction) => mediator ! publishTransaction(TransactionMessage(transaction, nodeId))
    case CheckPowSolution(solution) =>
      val senderRef = sender
      blockchain.askAndMap(Blockchain.GetLastHash) { hash: String =>
        miner.tell(Miner.Validate(hash, solution), senderRef)
      }
    case AddBlock(proof, transactions, timestamp) =>
      self.askAndMap(CheckPowSolution(proof)) { _: String =>
        broker ! Broker.DiffTransaction(transactions)
        blockchain ! Blockchain.AddBlockCommand(transactions, proof, timestamp)
        miner ! ReadyYourself
      }

    case Mine =>
      blockchain.askAndMap(Blockchain.GetLastHash) { hash: String =>
        val proofOfWorkFuture: Future[Long] = (miner ? Miner.Mine(hash)).mapTo[Future[Long]].flatten
        proofOfWorkFuture givenSuccess { rewardMiningAndAddBlock }
      }
    case GetTransactions => broker forward Broker.GetPendingTransactions
    case GetStatus       => blockchain forward Blockchain.GetChain
    case ReadMessages =>
      val senderRef = sender
      blockchain.askAndMap(Blockchain.CollectReceivedMessages) { receivedMessages: List[MessageTransaction] =>
        keeper.tell(KeeperOfKeys.ReadMessages(receivedMessages), senderRef)
      }
  }

  private def rewardMiningAndAddBlock(solution: Long): Unit = {

    val time = System.currentTimeMillis()

    broker ! Broker.AddTransactionToPending(createMiningRewardTransaction(nodeId))
    broker.askAndMap(Broker.GetPendingTransactions) { transactions: List[Transaction] =>
      mediator ! publishNewBlock(AddBlock(solution, transactions, time))
    }
    miner ! Miner.ReadyYourself
  }
}

object Node extends ActorName {
  sealed trait NodeMessage

  case class AddTransaction(message: MessageTransaction) extends NodeMessage

  case class TransactionMessage(message: MessageTransaction, nodeId: String) extends NodeMessage

  private case class CheckPowSolution(solution: Long) extends NodeMessage

  case class AddBlock(proof: Long, transactions: List[Transaction], timestamp: Long) extends NodeMessage

  case object GetTransactions extends NodeMessage

  case object Mine extends NodeMessage

  case object GetStatus extends NodeMessage

  case class GetRecipientPublicKey(recipientNodeId: String) extends NodeMessage
  case object ReadMessages                                  extends NodeMessage

  def props(nodeId: String, mediator: ActorRef): Props = Props(new Node(nodeId, mediator))

  //TODO edit amount to be configurable
  def createMiningRewardTransaction(nodeId: String): Transaction = MiningReward("theBank", nodeId)
}
