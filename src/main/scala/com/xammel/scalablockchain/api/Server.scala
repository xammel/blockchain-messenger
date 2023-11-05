package com.xammel.scalablockchain.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.xammel.scalablockchain.actors.Node

import scala.concurrent.Await
import scala.concurrent.duration.Duration
object Server extends App with NodeRoutes {

  //TODO review this args parsing
  private val address = if (args.length > 0) args(0) else "localhost"
  private val port    = if (args.length > 1) args(1).toInt else 8080

  implicit val system: ActorSystem = ActorSystem("scala-blockchain")

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val node: ActorRef = system.actorOf(Node.props("scalaBlockchainNode0"))

  private lazy val routes: Route = statusRoutes ~ transactionRoutes ~ mineRoutes

  Http().bindAndHandle(routes, address, port)

  println(s"Server online at http://$address:$port/")

  Await.result(system.whenTerminated, Duration.Inf)

}
