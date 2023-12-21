package com.xammel.scalablockchain.actors

import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.pubsub.DistributedPubSub
import akka.remote.testkit.MultiNodeSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.actor._
import com.xammel.scalablockchain.models.EmptyChain

import scala.concurrent.duration._

class ReactivePaymentProcessorMultiNode
    extends MultiNodeSpec(ReactivePaymentMultiNodeConfig)
    with ScalaTestMultiNodeSpec
    with ScalaFutures {

  // setup a ScalaTest patienceConfig for use in subsequent tests that involve Futures
  override implicit val patienceConfig = PatienceConfig(scaled(Span(15, Seconds)))

  import ReactivePaymentMultiNodeConfig._

  // tell the testkit how many nodes we expect to participate at first
  override def initialParticipants = 2

  "A Reactive Payment Processor" must {

    var nodeActor: Option[ActorRef] = None

    "start all nodes" in within(15.seconds) {

      // code that isn't part of a `runOn` block will run on *all* nodes

      // subscribe to MemberUp cluster events that we can leverage to assert that all nodes are up
      Cluster(system).subscribe(testActor, classOf[MemberUp])

      // the subscription will result in us receiving an `CurrentClusterState` snapshot as the first event, which we must handle
      expectMsgClass(classOf[CurrentClusterState])

      // instruct the ActorSystem on each node to join the cluster
      Cluster(system) join node(node1).address

      // bootstrap the application on node 1
      lazy val mediator = DistributedPubSub(system).mediator
      runOn(node1) {
//        nodeActor = Some(system.actorOf(Node.props(node1.name, mediator), Node.actorName))
        }

      // bootstrap the application on node 2
      runOn(node2) {
        nodeActor = Some(system.actorOf(Node.props(node2.name, mediator), Node.actorName))
      }

      // bootstrap the application on node 3
      // we also keep a reference to this ReactivePaymentProcessor for further tests
//      runOn(node3) {
//        nodeActor = Some(system.actorOf(Node.props(node3.name, mediator), Node.actorName))
//      }

      // verify that all nodes have reached the "Up" state by collecting MemberUp events
      receiveN(2).collect { case MemberUp(m) => m.address }.toSet should be(
        Set(node(node1).address, node(node2).address)
      )

      // enter a new "all-up" barrier
      testConductor.enter("all-up")
    }

    "get status" in within(5.seconds) {
      runOn(node1){
//        println(s"\n\n\n ${nodeActor.get.path}")
        lazy val mediator = DistributedPubSub(system).mediator
        val nodeActorr = system.actorOf(Node.props(node1.name, mediator), Node.actorName)
        nodeActorr ! Node.GetBalance(node1.name)
        expectMsg(0)
      }
      enterBarrier("finished")
    }

    "try something else" in within(5.seconds) {
      runOn(node1) {
        println(s"\n\n\n ${nodeActor.get.path}")
        nodeActor.get ! Node.GetBalance(node1.name)
//        expectMsg(0)
      }
      enterBarrier("finished2")
    }
  }

}

class ReactivePaymentMultiJvmNode1 extends ReactivePaymentProcessorMultiNode
class ReactivePaymentMultiJvmNode2 extends ReactivePaymentProcessorMultiNode
//class ReactivePaymentMultiJvmNode3 extends ReactivePaymentProcessorMultiNode
