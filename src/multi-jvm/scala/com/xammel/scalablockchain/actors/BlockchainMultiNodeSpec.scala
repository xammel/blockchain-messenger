package com.xammel.scalablockchain.actors

import akka.actor.{ActorRef, _}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.pubsub.DistributedPubSub
import akka.remote.transport.ActorTransportAdapter.AskTimeout
import com.xammel.scalablockchain.models._

import scala.concurrent.Await
import scala.concurrent.duration._

class BlockchainMultiNodeSpec extends ScalaTestMultiNodeSpec {

  import BlockchainMultiNodeConfig._

  def getActor(actorName: ActorName): ActorRef = {
    val name        = actorName.actorName
    val queryString = s"/user/${if (name == Node.actorName) name else s"/${Node.actorName}/$name"}"
    Await.result(
      system
        .actorSelection(queryString)
        .resolveOne(),
      Duration.Inf
    )
  }

  def awaitMinerToBeReady(minerActor: ActorRef) = {
    //Wait for mining to start
    Thread.sleep(1000)
    awaitAssert(
      {
        println("Waiting for miner to be ready")
        minerActor ! Miner.GetStatus
        expectMsg(Miner.Ready) == Miner.Ready
      },
      Duration(1, DAYS)
    )
    // Wait for update to be published
    Thread.sleep(1000)
  }

  "A blockchain cluster" must {

    "start all nodes" in within(15.seconds) {

      Cluster(system).subscribe(testActor, classOf[MemberUp])
      expectMsgClass(classOf[CurrentClusterState])

      val firstAddress  = node(node1).address
      val secondAddress = node(node2).address

      Cluster(system).join(firstAddress)

      lazy val mediator = DistributedPubSub(system).mediator

      runOn(node1) {
        system.actorOf(Node.props(node1.name, mediator), Node.actorName)
      }

      runOn(node2) {
        system.actorOf(Node.props(node2.name, mediator), Node.actorName)
      }

      receiveN(2).collect { case MemberUp(m) => m.address }.toSet should be(Set(firstAddress, secondAddress))

      Cluster(system).unsubscribe(testActor)

      testConductor.enter("all-up")
    }

    "publish new blocks to other nodes when blocks are mined" in {

      // Check initial chain is empty on node 2
      runOn(node2) {
        getActor(Node) ! Node.GetStatus
        expectMsg(EmptyChain)
      }

      // Mine new block on node 1
      runOn(node1) {
        getActor(Node) ! Node.Mine
        // Await for mining to finish on node 1
        awaitMinerToBeReady(getActor(Miner))
      }

      testConductor.enter("mining-finished")

      // Check the blockchain on node 2 includes the newly mined block from node 1
      runOn(node2) {
        getActor(Node) ! Node.GetStatus
        val chain: NonEmptyChain = expectMsgType[NonEmptyChain]

        chain.blocks.length shouldBe 2
        chain.blocks.head shouldBe GenesisBlock
        chain.blocks.last shouldBe a[PopulatedBlock]
      }

      testConductor.enter("status-test-done")
    }
  }

}

class BlockchainSpecMultiJvmNode1 extends BlockchainMultiNodeSpec
class BlockchainSpecMultiJvmNode2 extends BlockchainMultiNodeSpec
