package com.xammel.scalablockchain.actors

import akka.actor.Props
import com.xammel.scalablockchain.models._

class Broker(nodeId: String) extends ScalaBlockchainActor[Broker.BrokerMessage] {

  import Broker._

  private var pending: List[Transaction]      = Nil
  private def pendingMessageTransactions      = pending.fromNode(nodeId).collectMessageTransactions
  private def pendingMessageTransactionsValue = pendingMessageTransactions.map(_.value).sum
  private def pendingMiningRewards            = pending.toNode(nodeId).collectMiningRewards
  private def pendingMiningRewardsValue       = pendingMiningRewards.map(_.value).sum

  private def balance(chain: Chain, nodeId: String): Long = {
    val allChainTransactions                      = chain.allTransactions
    val receivedMiningRewards: List[MiningReward] = allChainTransactions.toNode(nodeId).collectMiningRewards
    val receivedTokens: Long                      = receivedMiningRewards.map(_.value).sum + pendingMiningRewardsValue
    val sentMessageTransactions                   = allChainTransactions.fromNode(nodeId).collectMessageTransactions
    val spentTokens: Long                         = sentMessageTransactions.map(_.value).sum + pendingMessageTransactionsValue
    receivedTokens - spentTokens
  }

  override def handleMessages: ReceiveType[BrokerMessage] = {
    case AddTransactionToPending(transaction) =>
      pending = pending :+ transaction
      log.info(s"Added transaction ${transaction.id} to pending transactions")
    case GetPendingTransactions =>
      log.info(s"Getting pending transactions")
      log.info(pending.toString)
      sender ! pending
    case DiffTransaction(externalTransactions) =>
      pending = pending diff externalTransactions
    case CalculateBalance(chain, nodeId)       => sender ! balance(chain, nodeId)
  }

}

object Broker extends ActorName {
  sealed trait BrokerMessage
  case class AddTransactionToPending(transaction: Transaction) extends BrokerMessage
  case object GetPendingTransactions                           extends BrokerMessage
  case class DiffTransaction(transactions: List[Transaction])  extends BrokerMessage
  case class CalculateBalance(chain: Chain, nodeId: String)    extends BrokerMessage

  def props(nodeId: String): Props = Props(new Broker(nodeId))
}
