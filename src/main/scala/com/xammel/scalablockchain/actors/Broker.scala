package com.xammel.scalablockchain.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import com.xammel.scalablockchain.models.{ActorName, Transaction}

class Broker extends Actor with ActorLogging {

  import Broker._

  //TODO review default impl
  //TODO does this need to be mutable?
  private var pending: List[Transaction] = Nil

  override def receive: Receive = {
    case AddTransactionToPending(transaction) =>
      pending = pending :+ transaction
      log.info(s"Added transaction ${transaction.transactionId} to pending transactions")
    case GetPendingTransactions =>
      log.info(s"Getting pending transactions")
      sender() ! pending
    case DiffTransaction(externalTransactions) => pending = pending diff externalTransactions
    //TODO unsure on the use of this
    case SubscribeAck(Subscribe("transaction", None, `self`)) => log.info("subscribing")
  }
}

object Broker extends ActorName {
  sealed trait BrokerMessage
  case class AddTransactionToPending(transaction: Transaction) extends BrokerMessage
  case object GetPendingTransactions                           extends BrokerMessage
  case class DiffTransaction(transactions: List[Transaction])  extends BrokerMessage

  val props: Props = Props(new Broker)
}
