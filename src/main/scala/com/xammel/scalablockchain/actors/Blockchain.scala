package com.xammel.scalablockchain.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence._
import com.xammel.scalablockchain.actors.Blockchain._
import com.xammel.scalablockchain.models.{Chain, Transaction}

class Blockchain(chain: Chain, nodeId: String) extends PersistentActor with ActorLogging {

  override def persistenceId: String = s"chainer-$nodeId"

  var state = State(chain)

  override def receiveRecover: Receive = {
    case SnapshotOffer(metadata, snapshot: State) =>
      log.info(
        s"Recovering from snapshot ${metadata.sequenceNr} at block ${snapshot.chain.mostRecentBlocksIndex}"
      )
      state = snapshot
    case RecoveryCompleted    => log.info("Recovery completed")
    case event: AddBlockCommand => updateState(event)
  }

  override def receiveCommand: Receive = snapshotResponseHandling orElse {
    case command: AddBlockCommand => persist(command)(updateState(_))
    case GetChain                 => sender() ! state.chain
    case GetLastHash              => sender() ! state.chain.mostRecentBlocksHash
    case GetLastIndex             => sender() ! state.chain.mostRecentBlocksIndex
  }

  private def snapshotResponseHandling: Receive = {
    case SaveSnapshotSuccess(metadata) =>
      log.info(s"Snapshot ${metadata.sequenceNr} saved successfully")
    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"Error saving snapshot ${metadata.sequenceNr}: ${reason.getMessage}")
  }

  def updateState(command: AddBlockCommand) = {
    state = State(state.chain.addBlock(command.transactions, command.proof))
    log.info(
      s"Added block ${state.chain.mostRecentBlocksIndex} containing ${command.transactions.length} transactions"
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

  case class State(chain: Chain)

  def props(chain: Chain, nodeId: String): Props = Props(new Blockchain(chain, nodeId))
}
