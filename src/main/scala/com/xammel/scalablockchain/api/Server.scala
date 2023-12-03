package com.xammel.scalablockchain.api

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import com.xammel.scalablockchain.actors.Node
import com.xammel.scalablockchain.cluster.ClusterListener

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Server extends App with NodeRoutes {

  implicit val system: ActorSystem             = ActorSystem("scala-blockchain")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config: Config = ConfigFactory.load()
  val address        = config.getString("http.ip")
  val port           = config.getInt("http.port")
  val nodeId         = config.getString("scalablockchain.node.id")

  private lazy val routes: Route = statusRoutes ~ transactionRoutes ~ mineRoutes ~ messengerRoutes ~ balanceRoutes

  val cluster            = Cluster(system)
  val mediator: ActorRef = DistributedPubSub(system).mediator
  val node               = system.actorOf(Node.props(nodeId, mediator), Node.actorName)

  val clusterListener =
    system.actorOf(ClusterListener.props(nodeId, cluster), ClusterListener.actorName)

  Http().bindAndHandle(routes, address, port)

  println(s"Server online at http://$address:$port/")

  Await.result(system.whenTerminated, Duration.Inf)

}
