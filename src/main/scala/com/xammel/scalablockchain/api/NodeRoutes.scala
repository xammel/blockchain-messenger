package com.xammel.scalablockchain.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.xammel.scalablockchain.actors.Node
import com.xammel.scalablockchain.actors.Node._
import com.xammel.scalablockchain.json.JsonSupport
import com.xammel.scalablockchain.models.{Chain, MessageTransaction}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

trait NodeRoutes extends SprayJsonSupport with JsonSupport {

  implicit def system: ActorSystem

  def node: ActorRef
  val nodeId: String
  def clusterListener: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val messengerRoutes: Route = path(NodeRoutes.myMessages) { get { tellNodeToReadMessages } }

  lazy val statusRoutes: Route = path(NodeRoutes.status) { concat { get { askStatusFromNode } } }

  lazy val balanceRoutes: Route = path(NodeRoutes.balance) { concat { get { askBalanceFromNode } } }

  lazy val transactionRoutes: Route = path(NodeRoutes.messages) {
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
    val transactionsRetrieved: Future[List[MessageTransaction]] =
      (node ? GetTransactions).mapTo[List[MessageTransaction]]
    onSuccess(transactionsRetrieved) { transactions =>
      complete(transactions)
    }
  }

  private def tellNodeToAddTransaction: Route = {
    entity(as[MessageTransaction]) { transaction =>
      node ! AddTransaction(transaction)
      complete(StatusCodes.OK)
    }
  }

  private def tellNodeToMine: Route = {
    node ! Mine
    complete(StatusCodes.OK)
  }

  private def tellNodeToReadMessages: Route = {
    val messagesRetrieved: Future[List[MessageTransaction]] = (node ? ReadMessages).mapTo[List[MessageTransaction]]
    onSuccess(messagesRetrieved) { messages =>
      complete(messages)
    }
  }

  private def askBalanceFromNode: Route = {
    val balanceFuture: Future[Long] =
      (node ? GetBalance(nodeId)).mapTo[Long]
    onSuccess(balanceFuture) { balance =>
      complete(balance.toString)
    }
  }

}

object NodeRoutes {
  // URL paths
  val status     = "status"
  val mine       = "mine"
  val messages   = "messages"
  val myMessages = "my-messages"
  val balance    = "balance"
}
