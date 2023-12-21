package com.xammel.scalablockchain.actors

import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object ReactivePaymentMultiNodeConfig extends MultiNodeConfig {

  // define the test roles that need to be filled when running the test
  // in short, we define a set of nodes that we want to work with
  val node1 = role("node1")
  val node2 = role("node2")
  val node3 = role("node3")

  // enable the test transport that allows to do fancy things such as blackhole, throttle, etc.
  testTransport(on = true)

  // configuration for node1
  nodeConfig(node1)(ConfigFactory.parseString(
    """
      |akka.cluster.roles=[bank-A]
      |akka.persistence.journal.leveldb.dir = "target/journal-A"
    """.stripMargin))

  // configuration for node2
  nodeConfig(node2)(ConfigFactory.parseString(
    """
      |akka.cluster.roles=[bank-B]
      |akka.persistence.journal.leveldb.dir = "target/journal-B"
    """.stripMargin))

  // configuration for node3
  nodeConfig(node3)(ConfigFactory.parseString(
    """
      |akka.cluster.roles=[bank-C]
      |akka.persistence.journal.leveldb.dir = "target/journal-C"
    """.stripMargin))

  // common configuration for all nodes
  commonConfig(ConfigFactory.parseString(
    """
      |akka.loglevel=INFO
      |akka.actor.provider = cluster
      |akka.remote.artery.enabled = on
      |akka.coordinated-shutdown.run-by-jvm-shutdown-hook = off
      |akka.coordinated-shutdown.terminate-actor-system = off
      |akka.cluster.run-coordinated-shutdown-when-down = off
      |akka.persistence.journal.plugin = "akka.persistence.journal.leveldb"
    """.stripMargin))
}
