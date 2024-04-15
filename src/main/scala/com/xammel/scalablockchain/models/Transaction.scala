package com.xammel.scalablockchain.models

import scala.util.Random

sealed trait Transaction {
  //  TODO make this generation a method to ensure no possibility of duplicate IDs
  val transactionId: String = Random.alphanumeric.take(6).mkString
  val originator: String
  val beneficiary: String
  val value: Long
}

object Transaction {

  implicit class TransactionHelpers(transactions: List[Transaction]) {
    def collectMessageTransactions: List[MessageTransaction] = transactions.collect { case m: MessageTransaction => m }

    def collectMiningRewards: List[MiningReward] = transactions.collect { case m: MiningReward => m }

    def fromNode(nodeId: String): List[Transaction] = transactions.filter(_.originator == nodeId)

    def toNode(nodeId: String): List[Transaction] = transactions.filter(_.beneficiary == nodeId)
  }

}

case class MiningReward(originator: String, beneficiary: String) extends Transaction {
  override val value: Long = 10
}

case class MessageTransaction(
    originator: String,
    beneficiary: String,
    message: String
) extends Transaction {
  override val value: Long = 1
  private lazy val id      = this.transactionId

  def copy(message: String): MessageTransaction =
    new MessageTransaction(
      originator = this.originator,
      beneficiary = this.beneficiary,
      message = message
    ) {
      override val transactionId: String = id
    }

  def setTransactionId(id: String): MessageTransaction =
    new MessageTransaction(
      originator = this.originator,
      beneficiary = this.beneficiary,
      message = this.message
    ) {
      override val transactionId: String = id
    }
}
