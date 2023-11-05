package com.xammel.scalablockchain.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.xammel.scalablockchain.actors.Node
import com.xammel.scalablockchain.actors.Node.{AddTransaction, GetTransactions, Mine}
import com.xammel.scalablockchain.blockchain.{Chain, Transaction}
import spray.json._
import scala.concurrent.Future
import scala.concurrent.duration._
import com.xammel.scalablockchain.utils.JsonSupport._

  trait NodeRoutes extends SprayJsonSupport {

    implicit def system: ActorSystem

    def node: ActorRef
//    def clusterManager: ActorRef

    implicit lazy val timeout = Timeout(5.seconds)

    lazy val statusRoutes: Route = pathPrefix("status") {
      concat(
        pathEnd {
          concat(
            get {
              println("inside get")
              val statusFuture: Future[Chain] = (node ? Node.GetStatus).mapTo[Chain]
              onSuccess(statusFuture) { status =>
                complete(StatusCodes.OK, status)
              }
            }
          )
        })
//        ,
//        pathPrefix("members") {
//          concat(
//            pathEnd {
//              concat(
//                get {
//                  val membersFuture: Future[List[String]] = (clusterManager ? GetMembers).mapTo[List[String]]
//                  onSuccess(membersFuture) { members =>
//                    complete(StatusCodes.OK, members)
//                  }
//                }
//              )
//            }
//          )
//        }
//      )
    }

    lazy val transactionRoutes: Route = pathPrefix("transactions") {
      concat(
        pathEnd {
          concat(
            get {
              val transactionsRetrieved: Future[List[Transaction]] =
                (node ? GetTransactions).mapTo[List[Transaction]]
              onSuccess(transactionsRetrieved) { transactions =>
                complete(transactions.toList)
              }
            },
            post {
              entity(as[Transaction]) { transaction =>
                val transactionCreated: Future[Int] =
                  (node ? AddTransaction(transaction)).mapTo[Int]
                onSuccess(transactionCreated) { done =>
                  complete((StatusCodes.Created, done.toString))
                }
              }
            }
          )
        }
      )
    }

    lazy val mineRoutes: Route = pathPrefix("mine") {
      concat(
        pathEnd {
          concat(
            get {
              node ! Mine
              complete(StatusCodes.OK)
            }
          )
        }
      )
    }

  }
