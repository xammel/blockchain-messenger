package com.xammel.scalablockchain.actors

import akka.actor.ActorSystem
import akka.cluster.pubsub.DistributedPubSub
import akka.remote.testkit.MultiNodeConfig
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

abstract class ActorSpec(name: String)
    extends TestKit(ActorSystem(name))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  lazy val mediator = DistributedPubSub(system).mediator
  lazy val node     = system.actorOf(Node.props("testNode", mediator))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}
