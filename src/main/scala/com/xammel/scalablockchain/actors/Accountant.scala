package com.xammel.scalablockchain.actors

import akka.actor.Props
import com.xammel.scalablockchain.models.{ActorName, Chain, MiningReward, ScalaBlockchainActor}

class Accountant extends ScalaBlockchainActor[Accountant.AccountantMessage] {
  import Accountant._

  private def balance(chain: Chain, nodeId: String): Long = {
    val receivedMiningRewards: List[MiningReward] = chain.miningRewardsTo(nodeId)
    val receivedTokens: Long                      = receivedMiningRewards.map(_.value).sum
    val sentMessageTransactions                   = chain.messageTransactionsFrom(nodeId)
    val spentTokens: Long                         = sentMessageTransactions.map(_.value).sum
    receivedTokens - spentTokens
  }

  override def handleMessages: ReceiveType[Accountant.AccountantMessage] = {
    //TODO doesn't currently account for transactions pending
    case CalculateBalance(chain, nodeId) => sender ! balance(chain, nodeId)
  }

}

object Accountant extends ActorName {
  sealed trait AccountantMessage
  case class CalculateBalance(chain: Chain, nodeId: String) extends AccountantMessage

  val props: Props = Props(new Accountant)
}
