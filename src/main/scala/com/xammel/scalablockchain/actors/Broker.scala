package com.xammel.scalablockchain.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import com.xammel.scalablockchain.actors.Broker._
import com.xammel.scalablockchain.models.Transaction

class Broker extends Actor with ActorLogging {

  //TODO review default impl
  //TODO does this need to be mutable?
  var pending: List[Transaction] = Nil

  override def receive: Receive = {
    case AddTransactionToPending(transaction) =>
      pending = pending :+ transaction
      log.info(s"Added transaction ${transaction.transactionId} to pending transactions")
    case GetPendingTransactions =>
      log.info(s"Getting pending transactions")
      sender() ! pending
    case DiffTransaction(externalTransactions) => {
      pending = pending diff externalTransactions
    }
    case ClearPendingTransactions =>
      pending = Nil
      log.info("Cleared pending transaction List")
    case SubscribeAck(Subscribe("transaction", None, `self`)) =>
      log.info("subscribing")
  }
}

object Broker {
  sealed trait BrokerMessage
  case class AddTransactionToPending(transaction: Transaction) extends BrokerMessage
  case object GetPendingTransactions                           extends BrokerMessage
  case object ClearPendingTransactions                         extends BrokerMessage
  case class DiffTransaction(transactions: List[Transaction]) extends BrokerMessage

  val props: Props = Props(new Broker)
}
