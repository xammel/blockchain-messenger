package com.xammel.scalablockchain.actors

import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object BlockchainMultiNodeConfig extends MultiNodeConfig {

  val node1 = role("node1")
  val node2 = role("node2")

  testTransport(on = true)

  nodeConfig(node1)(ConfigFactory.parseString("""
      |akka.cluster.roles=[bank-A]
      |akka.persistence.journal.leveldb.dir = "target/journal-A"
    """.stripMargin))

  nodeConfig(node2)(ConfigFactory.parseString("""
      |akka.cluster.roles=[bank-B]
      |akka.persistence.journal.leveldb.dir = "target/journal-B"
      |""".stripMargin))

  commonConfig(ConfigFactory.parseString("""
      |akka.loglevel=INFO
      |akka.actor.provider = cluster
      |akka.remote.artery.enabled = on
      |akka.coordinated-shutdown.run-by-jvm-shutdown-hook = off
      |akka.coordinated-shutdown.terminate-actor-system = off
      |akka.cluster.run-coordinated-shutdown-when-down = off
      |akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
    """.stripMargin))
}
