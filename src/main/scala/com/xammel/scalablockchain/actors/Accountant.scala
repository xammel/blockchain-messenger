package com.xammel.scalablockchain.actors

import akka.actor.Props
import com.xammel.scalablockchain.models.{ActorName, Chain, MiningReward, ScalaBlockchainActor}

class Accountant(nodeId: String) extends ScalaBlockchainActor[Accountant.AccountantMessage] {
  import Accountant._

  private def balance(chain: Chain): Long = {
    val receivedMiningRewards: List[MiningReward] = chain.miningRewardsTo(nodeId)
    val receivedTokens: Long                      = receivedMiningRewards.map(_.value).sum
    val sentMessageTransactions                   = chain.messageTransactionsFrom(nodeId)
    val spentTokens: Long                         = sentMessageTransactions.map(_.value).sum
    receivedTokens - spentTokens
  }

  override def handleMessages: ReceiveType[Accountant.AccountantMessage] = { case CalculateBalance(chain) =>
    sender ! balance(chain)
  }

}

object Accountant extends ActorName {
  sealed trait AccountantMessage
  case class CalculateBalance(chain: Chain) extends AccountantMessage

  def props(nodeId: String): Props = Props(new Accountant(nodeId))
}
