package com.xammel.scalablockchain.actors

import com.xammel.scalablockchain.models.{ActorName, Chain, ScalaBlockchainActor}

class Accountant extends ScalaBlockchainActor[Accountant.AccountantMessage] {
  override def handleMessages: ReceiveType[Accountant.AccountantMessage] = ???
}

object Accountant extends ActorName {
  sealed trait AccountantMessage
  case class CalculateBalance(chain: Chain) extends AccountantMessage
}
