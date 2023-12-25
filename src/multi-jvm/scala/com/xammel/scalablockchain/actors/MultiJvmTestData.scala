package com.xammel.scalablockchain.actors

import com.xammel.scalablockchain.actors.BlockchainMultiNodeConfig._
import com.xammel.scalablockchain.models.MessageTransaction

object MultiJvmTestData {

  lazy val testMessage: MessageTransaction =
    MessageTransaction(originator = node1.name, beneficiary = node2.name, message = "hello there node2")

}
