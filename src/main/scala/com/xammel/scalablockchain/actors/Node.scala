package com.xammel.scalablockchain.actors

import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.pattern.ask
import akka.util.Timeout
import com.xammel.scalablockchain.actors.Miner.ReadyYourself
import com.xammel.scalablockchain.actors.Node._
import com.xammel.scalablockchain.blockchain.{EmptyChain, Transaction}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class Node(nodeId: String) extends Actor with ActorLogging {

  implicit lazy val timeout = Timeout(5.seconds)

  val broker     = context.actorOf(Broker.props)
  val miner      = context.actorOf(Miner.props)
  val blockchain = context.actorOf(Blockchain.props(EmptyChain, nodeId))

  miner ! ReadyYourself

  override def receive: Receive = {
    case AddTransaction(transaction) =>
      val node = sender()
      broker ! Broker.AddTransactionToPending(transaction)
      (blockchain ? Blockchain.GetLastIndex).mapTo[Long] onComplete {
        case Success(index) => node ! (index + 1)
        case Failure(e)     => node ! akka.actor.Status.Failure(e)
      }
    case CheckPowSolution(solution) =>
      val node = sender()
      (blockchain ? Blockchain.GetLastHash).mapTo[String] onComplete {
        case Success(hash: String) => miner ! miner.tell(Miner.Validate(hash, solution), node)
        case Failure(e)            => node ! akka.actor.Status.Failure(e)
      }
    case AddBlock(proof) =>
      val node = sender()
      (self ? CheckPowSolution(proof)) onComplete {
        case Success(_) =>
          (broker ? Broker.GetPendingTransactions).mapTo[List[Transaction]] onComplete {
            case Success(transactions) =>
              blockchain.tell(
                Blockchain.AddBlockCommand(transactions, proof, System.currentTimeMillis()),
                node
              ) //TODO review this timestamp
            case Failure(e) => node ! Status.Failure(e)
          }
          broker ! Broker.ClearPendingTransactions
        case Failure(e) => node ! Status.Failure(e)
      }
    case Mine =>
      val node = sender()
      (blockchain ? Blockchain.GetLastHash).mapTo[String] onComplete {
        case Success(hash) =>
          //TODO should this not be .mapTo[Long]? this results in Future[Future[Long]]
          (miner ? Miner.Mine(hash)).mapTo[Future[Long]] onComplete {
            case Success(solution) => waitForSolution(solution)
            case Failure(e)        => log.error(s"Error finding PoW solution: ${e.getMessage}")
          }
        case Failure(e) => node ! akka.actor.Status.Failure(e)
      }
    case GetTransactions   => broker forward Broker.GetPendingTransactions
    case GetStatus         => blockchain forward Blockchain.GetChain
    case GetLastBlockIndex => blockchain forward Blockchain.GetLastIndex
    case GetLastBlockHash  => blockchain forward Blockchain.GetLastHash
  }

  //TODO can this just return Unit?
  def waitForSolution(solution: Future[Long]): Future[Unit] = Future {
    solution onComplete {
      //TODO should there be a criteria which only allows blocks to be mined if there
      // exist pending transactions. otherwise blocks can be mined with just the 1 mining reward
      // transaction in them
      case Success(proof) =>
        broker ! Broker.AddTransactionToPending(createMiningRewardTransaction(nodeId))
        self ! AddBlock(proof)
        miner ! Miner.ReadyYourself
      case Failure(e) => log.error(s"Error finding PoW solution: ${e.getMessage}")
    }
  }
}

object Node {
  sealed trait NodeMessage

  case class AddTransaction(transaction: Transaction) extends NodeMessage

  case class CheckPowSolution(solution: Long) extends NodeMessage

  case class AddBlock(proof: Long) extends NodeMessage

  case object GetTransactions extends NodeMessage

  case object Mine extends NodeMessage

  case object StopMining extends NodeMessage

  case object GetStatus extends NodeMessage

  case object GetLastBlockIndex extends NodeMessage

  case object GetLastBlockHash extends NodeMessage

  def props(nodeId: String): Props = Props(new Node(nodeId))

  //TODO edit amount to be configurable
  def createMiningRewardTransaction(nodeId: String) = Transaction("theBank", nodeId, 100)
}
