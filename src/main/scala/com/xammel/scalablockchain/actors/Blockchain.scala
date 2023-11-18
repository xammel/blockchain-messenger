package com.xammel.scalablockchain.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence._
import com.xammel.scalablockchain.actors.Blockchain._
import com.xammel.scalablockchain.models.{Chain, Transaction}

//TODO if facing long actor recovery times, could implement snapshotting.
class Blockchain(chain: Chain, nodeId: String) extends PersistentActor with ActorLogging {

  override def persistenceId: String = s"chainer-$nodeId"

  var state: Chain = chain

  override def receiveRecover: Receive = {
    case RecoveryCompleted      => log.info("Recovery completed")
    case event: AddBlockCommand => updateState(event)
  }

  override def receiveCommand: Receive = {
    case command: AddBlockCommand => persist(command)(updateState)
    case GetChain                 => sender() ! state
    case GetLastHash              => sender() ! state.mostRecentBlocksHash
    case GetLastIndex             => sender() ! state.mostRecentBlocksIndex
  }

  def updateState(command: AddBlockCommand) = {
    import command.{transactions, proof, timestamp}
    state = state.addBlock(transactions, proof, timestamp)
    log.info(
      s"Added block ${state.mostRecentBlocksIndex} containing ${command.transactions.length} transactions"
    )
  }

}
object Blockchain {

  sealed trait BlockchainCommand

  case class AddBlockCommand(transactions: List[Transaction], proof: Long, timestamp: Long)
      extends BlockchainCommand

  case object GetChain extends BlockchainCommand

  case object GetLastHash extends BlockchainCommand

  case object GetLastIndex extends BlockchainCommand

  def props(chain: Chain, nodeId: String): Props = Props(new Blockchain(chain, nodeId))
}
