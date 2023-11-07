package com.xammel.scalablockchain.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence._
import com.xammel.scalablockchain.actors.Blockchain._
import com.xammel.scalablockchain.blockchain.{Chain, ChainLink, Transaction}
class Blockchain(chain: Chain, nodeId: String) extends PersistentActor with ActorLogging {

  override def persistenceId: String = s"chainer-$nodeId"

  var state = State(chain)

  override def receiveRecover: Receive = {
    case SnapshotOffer(metadata, snapshot: State) => {
      log.info(s"Recovering from snapshot ${metadata.sequenceNr} at block ${snapshot.chain.index}")
      state = snapshot
    }
    case RecoveryCompleted    => log.info("Recovery completed")
    case event: AddBlockEvent => updateState(event)
  }

  override def receiveCommand: Receive = {
    case SaveSnapshotSuccess(metadata) =>
      log.info(s"Snapshot ${metadata.sequenceNr} saved successfully")
    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"Error saving snapshot ${metadata.sequenceNr}: ${reason.getMessage}")
    case AddBlockCommand(transactions: List[Transaction], proof: Long, timestamp) =>
      persist(AddBlockEvent(transactions, proof, timestamp)) { event =>
        updateState(event)
      }

      //TODO This is a workaround to wait until the state is persisted
      deferAsync(Nil) { _ =>
        saveSnapshot(state)
        sender() ! state.chain.index
      }

    case AddBlockCommand(_, _, _) => log.error("invalid add block command")
    case GetChain                 => sender() ! state.chain
    case GetLastHash              => sender() ! state.chain.previousHash
    case GetLastIndex             => sender() ! state.chain.index
  }

  def updateState(event: BlockchainEvent) = event match {
    case AddBlockEvent(transactions, proof, timestamp) =>
      state = State(
        ChainLink(state.chain.index + 1, proof, transactions, timestamp = timestamp) :: state.chain
      )
      log.info(s"Added block ${state.chain.index} containing ${transactions.size} transactions")
  }

}
object Blockchain {

  sealed trait BlockchainEvent

  case class AddBlockEvent(transactions: List[Transaction], proof: Long, timestamp: Long)
      extends BlockchainEvent

  sealed trait BlockchainCommand

  case class AddBlockCommand(transactions: List[Transaction], proof: Long, timestamp: Long)
      extends BlockchainCommand

  case object GetChain extends BlockchainCommand

  case object GetLastHash extends BlockchainCommand

  case object GetLastIndex extends BlockchainCommand

  case class State(chain: Chain)

  def props(chain: Chain, nodeId: String): Props = Props(new Blockchain(chain, nodeId))
}
