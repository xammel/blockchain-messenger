package com.xammel.scalablockchain.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.xammel.scalablockchain.actors.Node
import com.xammel.scalablockchain.actors.Node.{AddTransaction, GetTransactions, Mine, ReadMessages}
import com.xammel.scalablockchain.json.JsonSupport
import com.xammel.scalablockchain.models.{Chain, Message}

import scala.concurrent.Future
import scala.concurrent.duration._

trait NodeRoutes extends SprayJsonSupport with JsonSupport {

  implicit def system: ActorSystem

  def node: ActorRef
  def clusterListener: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val messengerRoutes: Route = path(NodeRoutes.messages) { get { tellNodeToReadMessages } }

  lazy val statusRoutes: Route = path(NodeRoutes.status) { get { askStatusFromNode } }

  lazy val transactionRoutes: Route = path(NodeRoutes.transactions) {
    concat(
      get {
        askTransactionsFromNode
      },
      post {
        tellNodeToAddTransaction
      }
    )
  }

  lazy val mineRoutes: Route = path(NodeRoutes.mine) { get { tellNodeToMine } }

  private def askStatusFromNode: Route = {
    val statusFuture: Future[Chain] = (node ? Node.GetStatus).mapTo[Chain]
    onSuccess(statusFuture) { status =>
      complete(StatusCodes.OK, status)
    }
  }

  private def askTransactionsFromNode: Route = {
    val transactionsRetrieved: Future[List[Message]] =
      (node ? GetTransactions).mapTo[List[Message]]
    onSuccess(transactionsRetrieved) { transactions =>
      complete(transactions)
    }
  }

  private def tellNodeToAddTransaction: Route = {
    entity(as[Message]) { transaction =>
      node ! AddTransaction(transaction)
      complete(StatusCodes.OK)
    }
  }

  private def tellNodeToMine: Route = {
    node ! Mine
    complete(StatusCodes.OK)
  }

  private def tellNodeToReadMessages: Route = {
    val messagesRetrieved: Future[List[String]] = (node ? ReadMessages).mapTo[List[String]]
    onSuccess(messagesRetrieved) { messages =>
      complete(messages)
    }
  }

}

object NodeRoutes {
  // URL paths
  val status       = "status"
  val transactions = "transactions"
  val mine         = "mine"
  val messages     = "messages"
}
