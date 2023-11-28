package com.xammel.scalablockchain.actors

import com.xammel.scalablockchain.models.ScalaBlockchainActor

class KeeperOfKeys extends ScalaBlockchainActor[KeeperOfKeys.KeeperMessage] {
  override def handleMessages: ReceiveType[KeeperOfKeys.KeeperMessage] = ???
}

object KeeperOfKeys {
  sealed trait KeeperMessage

  case object GetPublicKey
}
