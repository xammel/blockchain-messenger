package com.xammel.scalablockchain.actors

import akka.actor.{ActorRef, Props, Status}
import akka.pattern.ask
import akka.util.Timeout
import com.xammel.scalablockchain.actors.Miner.ReadyYourself
import com.xammel.scalablockchain.crypto.Crypto
import com.xammel.scalablockchain.models.{ActorName, EmptyChain, ScalaBlockchainActor, Transaction}
import com.xammel.scalablockchain.pubsub.PubSub._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
class Node(nodeId: String, mediator: ActorRef) extends ScalaBlockchainActor[Node.NodeMessage] {

  import Node._

  implicit lazy val timeout = Timeout(5.seconds)

  mediator ! subscribeNewBlock(self)
  mediator ! subscribeTransaction(self)
  mediator ! subscribeGetPublicKey(self)

  private val broker = context.actorOf(Broker.props, Broker.actorName)
  private val miner  = context.actorOf(Miner.props, Miner.actorName)
  private val blockchain =
    context.actorOf(Blockchain.props(EmptyChain, nodeId), Blockchain.actorName)

  miner ! ReadyYourself

  override def handleMessages: ReceiveType[NodeMessage] = {
    case GetRecipientPublicKey(recipientNodeId) if recipientNodeId == nodeId => {
      log.info(s" $nodeId , inside recipient receive fn")
      val keyPair = Crypto.generateKeyPair(nodeId)
      Crypto.base64Encode(keyPair.getPublic.getEncoded)
    }
    case GetRecipientPublicKey(recipientNodeId) if recipientNodeId != nodeId => log.info(s"$recipientNodeId did not match $nodeId")
    case TransactionMessage(transaction, messageNodeId) if messageNodeId != nodeId =>
      log.info(s"Received transaction message from $messageNodeId")
      broker ! Broker.AddTransactionToPending(transaction)
    case TransactionMessage(_, messageNodeId) if messageNodeId == nodeId => // ignore
    case AddTransaction(transaction) =>
      broker ! Broker.AddTransactionToPending(transaction)
      mediator ! publishTransaction(TransactionMessage(transaction, nodeId))
    case CheckPowSolution(solution) =>
      val node = sender()
      (blockchain ? Blockchain.GetLastHash).mapTo[String] onComplete {
        case Success(hash: String) => miner.tell(Miner.Validate(hash, solution), node)
        case Failure(e)            => node ! Status.Failure(e)
      }
    case AddBlock(proof, transactions, timestamp) =>
      val node = sender()
      (self ? CheckPowSolution(proof)) onComplete {
        case Success(_) =>
          broker ! Broker.DiffTransaction(transactions)
          blockchain.tell(
            Blockchain.AddBlockCommand(transactions, proof, timestamp),
            node
          )
          miner ! ReadyYourself
        case Failure(e) => node ! Status.Failure(e)
      }

    case Mine =>
      val node                           = sender()
      val lastHashFuture: Future[String] = (blockchain ? Blockchain.GetLastHash).mapTo[String]
      lastHashFuture onComplete {
        case Failure(e) => node ! Status.Failure(e)
        case Success(hash) =>
          val proofOfWorkFuture: Future[Long] =
            (miner ? Miner.Mine(hash)).mapTo[Future[Long]].flatten
          proofOfWorkFuture onComplete {
            case Success(solution) => rewardMiningAndAddBlock(solution)
            case Failure(e)        => log.error(s"Error finding PoW solution: ${e.getMessage}")
          }
      }
    case GetTransactions => broker forward Broker.GetPendingTransactions
    case GetStatus       => {
      //TODO REVERT
      (mediator ? publishGetPublicKey(GetRecipientPublicKey("node1"))).mapTo[String] onComplete {
        case Success(v) =>
          log.info("\n\n WOOHOO \n\n")
          log.info(v)
        case Failure(e) => Status.Failure(e)
      }
      blockchain forward Blockchain.GetChain
    }
    //TODO don't think these two are used
    case GetLastBlockIndex => blockchain forward Blockchain.GetLastIndex
    case GetLastBlockHash  => blockchain forward Blockchain.GetLastHash
  }

  private def rewardMiningAndAddBlock(solution: Long): Unit = {
    //TODO should there be a criteria which only allows blocks to be mined if there
    // exist pending transactions. otherwise blocks can be mined with just the 1 mining reward
    // transaction in them

    val node = sender()
    val time = System.currentTimeMillis()

    broker ! Broker.AddTransactionToPending(createMiningRewardTransaction(nodeId))
    (broker ? Broker.GetPendingTransactions).mapTo[List[Transaction]] onComplete {
      case Success(transactions) =>
        mediator ! publishNewBlock(AddBlock(solution, transactions, time))
      case Failure(exception) => node ! akka.actor.Status.Failure(exception)
    }
    miner ! Miner.ReadyYourself
  }
}

object Node extends ActorName {
  sealed trait NodeMessage

  case class AddTransaction(transaction: Transaction) extends NodeMessage

  case class TransactionMessage(transaction: Transaction, nodeId: String) extends NodeMessage

  case class CheckPowSolution(solution: Long) extends NodeMessage

  case class AddBlock(proof: Long, transactions: List[Transaction], timestamp: Long)
      extends NodeMessage

  case object GetTransactions extends NodeMessage

  case object Mine extends NodeMessage

  case object GetStatus extends NodeMessage

  //TODO these two aren't used yet...
  case object GetLastBlockIndex extends NodeMessage

  case object GetLastBlockHash extends NodeMessage

  case class GetRecipientPublicKey(recipientNodeId: String) extends NodeMessage

  def props(nodeId: String, mediator: ActorRef): Props = Props(new Node(nodeId, mediator))

  //TODO edit amount to be configurable
  def createMiningRewardTransaction(nodeId: String) = Transaction("theBank", nodeId, 100)
}
