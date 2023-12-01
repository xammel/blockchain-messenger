package com.xammel.scalablockchain.models

import scala.util.Random

sealed trait Transaction {
  //  TODO make this generation a method to ensure no possibility of duplicate IDs
  val transactionId: String = Random.alphanumeric.take(6).mkString
  val originator: String
  val beneficiary: String
  val value: Long
}

case class MiningReward(originator: String, beneficiary: String) extends Transaction {
  override val value: Long = 10 //TODO review
}

case class Message(
    originator: String,
    beneficiary: String,
    message: String
) extends Transaction {
  override val value: Long = 1 //TODO review
}
