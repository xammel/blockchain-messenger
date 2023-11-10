package com.xammel.scalablockchain.models

import scala.util.Random

case class Transaction(originator: String, beneficiary: String, value: Long) {
  //TODO make this generation a method to ensure no possibility of duplicate IDs
  val transactionId: String = Random.alphanumeric.take(6).mkString
}