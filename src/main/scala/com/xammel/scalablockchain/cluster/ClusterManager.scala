package com.xammel.scalablockchain.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.{Cluster, MemberStatus}
import com.xammel.scalablockchain.cluster.ClusterManager.GetMembers

class ClusterManager(nodeId: String) extends Actor with ActorLogging {

  val cluster: Cluster = Cluster(context.system)
  val listener: ActorRef =
    context.actorOf(ClusterListener.props(nodeId, cluster), "clusterListener")

  override def receive: Receive = { case GetMembers =>
    val activeMemberAddresses: Set[String] = cluster.state.members
      .filter(_.status == MemberStatus.up)
      .map(_.address.toString)

    sender() ! activeMemberAddresses
  }
}

object ClusterManager {

  sealed trait ClusterMessage
  case object GetMembers extends ClusterMessage

  def props(nodeId: String) = Props(new ClusterManager(nodeId))
}
