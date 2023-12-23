package com.xammel.scalablockchain.actors

import akka.actor.{ActorRef, _}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.pubsub.DistributedPubSub
import com.xammel.scalablockchain.actors.Node.{GetBalance, GetStatus, Mine}
import com.xammel.scalablockchain.models.{EmptyChain, NonEmptyChain, PopulatedBlock}

import scala.concurrent.duration._

class BlockchainMultiNodeSpec extends ScalaTestMultiNodeSpec {

  import NodeMultiNodeConfig._

  "A blockchain cluster" must {

    var nodeActor: Option[ActorRef] = None

    "start all nodes" in within(15.seconds) {

      Cluster(system).subscribe(testActor, classOf[MemberUp])
      expectMsgClass(classOf[CurrentClusterState])

      val firstAddress  = node(node1).address
      val secondAddress = node(node2).address

      Cluster(system).join(firstAddress)

      lazy val mediator = DistributedPubSub(system).mediator

      runOn(node1) {
        nodeActor = Some(system.actorOf(Node.props(node1.name, mediator)))
      }

      runOn(node2) {
        nodeActor = Some(system.actorOf(Node.props(node2.name, mediator)))
      }

      receiveN(2).collect { case MemberUp(m) => m.address }.toSet should be(Set(firstAddress, secondAddress))

      Cluster(system).unsubscribe(testActor)

      testConductor.enter("all-up")
    }

    "get status" in {
      runOn(node1) {
          nodeActor.get ! Mine
//          nodeActor.get ! GetStatus
//          expectMsg(EmptyChain)
      }

      Thread.sleep(15000)


      runOn(node2){
        nodeActor.get ! GetStatus
        expectMsgType[NonEmptyChain]
      }

      testConductor.enter("done-1")
    }

//    "get balance" in {
//      runOn(node1) {
//        awaitAssert {
//          nodeActor.get ! GetBalance(node1.name)
//          expectMsg(0L)
//        }
//      }
//    }
//    "get balance of another node" in {
//      runOn(node1) {
//        awaitAssert {
//          nodeActor.get ! GetBalance(node2.name)
//          expectMsg(0L)
//        }
//      }
//    }
  }

}

class BlockchainMultiJvmNode1 extends BlockchainMultiNodeSpec
class BlockchainMultiJvmNode2 extends BlockchainMultiNodeSpec
