package com.xammel.scalablockchain.actors

//
//import akka.actor.{Actor, Props}
//
//import scala.language.implicitConversions
//import org.scalatest.BeforeAndAfterAll
//import org.scalatest.matchers.should.Matchers
//import org.scalatest.wordspec.AnyWordSpecLike
//import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec, MultiNodeSpecCallbacks}
//import akka.testkit.ImplicitSender
//import com.xammel.scalablockchain.actors.Node
//object MultiNodeSampleConfig extends MultiNodeConfig {
//  val node1 = role("node1")
//  val node2 = role("node2")
//}
//
////class MultiNodeSampleSpecMultiJvmNode1 extends MultiNodeSample
////class MultiNodeSampleSpecMultiJvmNode2 extends MultiNodeSample
//
//object MultiNodeSample {
//  class Ponger extends Actor {
//    def receive = {
//      case "ping" => sender() ! "pong"
//    }
//  }
//}
//class NodeSpec extends MultiNodeSpec(MultiNodeSampleConfig) with STMultiNodeSpec with ImplicitSender  {
//
//  import MultiNodeSample._
//  import MultiNodeSampleConfig._
//
//  def initialParticipants = roles.size
//
//  "A MultiNodeSample" must {
//
//    "wait for all nodes to enter a barrier" in {
//      enterBarrier("startup")
//    }
//
//    "send to and receive from a remote node" in {
//      runOn(node1) {
//        enterBarrier("deployed")
//        val ponger = system.actorSelection(node(node2) / "user" / "ponger")
//        ponger ! "ping"
//        import scala.concurrent.duration._
//        expectMsg(10.seconds, "pong")
//      }
//
//      runOn(node2) {
//        system.actorOf(Props[Ponger](), "ponger")
//        enterBarrier("deployed")
//      }
//
//      enterBarrier("finished")
//    }
//  }
//}
//
///**
// * Hooks up MultiNodeSpec with ScalaTest
// */
//trait STMultiNodeSpec extends MultiNodeSpecCallbacks with AnyWordSpecLike with Matchers with BeforeAndAfterAll {
//  self: MultiNodeSpec =>
//
//  override def beforeAll() = multiNodeSpecBeforeAll()
//
//  override def afterAll() = multiNodeSpecAfterAll()
//
//  // Might not be needed anymore if we find a nice way to tag all logging from a node
//  override implicit def convertToWordSpecStringWrapper(s: String): WordSpecStringWrapper =
//    new WordSpecStringWrapper(s"$s (on node '${self.myself.name}', $getClass)")
//}

import com.xammel.scalablockchain.models.EmptyChain

class NodeSpec extends ActorSpec("NodeSpec") {

  "A Node actor" must {

    "send the status of the blockchain" in {
      node ! Node.GetStatus
      expectMsg(EmptyChain)
    }
  }

}
