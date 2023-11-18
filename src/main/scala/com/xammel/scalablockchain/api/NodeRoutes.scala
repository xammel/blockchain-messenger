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
import com.xammel.scalablockchain.cluster.ClusterManager
import com.xammel.scalablockchain.json.JsonSupport
import com.xammel.scalablockchain.models.{Chain, Transaction}

import scala.concurrent.Future
import scala.concurrent.duration._

trait NodeRoutes extends SprayJsonSupport with JsonSupport {

  implicit def system: ActorSystem

  def node: ActorRef
  def clusterManager: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val statusRoutes: Route = concat(
    path(NodeRoutes.status) { get { askStatusFromNode } },
    path(NodeRoutes.members) { get { askMembersFromClusterManager } }
  )

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

  private def askMembersFromClusterManager: Route = {
    val activeAddressesFuture: Future[Set[String]] =
      (clusterManager ? ClusterManager.GetMembers).mapTo[Set[String]]
    onSuccess(activeAddressesFuture) { addresses =>
      complete(StatusCodes.OK, addresses)
    }
  }

  private def askTransactionsFromNode: Route = {
    val transactionsRetrieved: Future[List[Transaction]] =
      (node ? GetTransactions).mapTo[List[Transaction]]
    onSuccess(transactionsRetrieved) { transactions =>
      complete(transactions.toList)
    }
  }

  private def tellNodeToAddTransaction: Route = {
    entity(as[Transaction]) { transaction =>
      node ! AddTransaction(transaction)
      complete(StatusCodes.OK)
    }
  }

  private def tellNodeToMine: Route = {
    node ! Mine
    complete(StatusCodes.OK)
  }

}

object NodeRoutes {
  // URL paths
  val status = "status"
  val members = "members"
  val transactions = "transactions"
  val mine = "mine"
}
