package com.xammel.scalablockchain.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.xammel.scalablockchain.actors.Broker._
import com.xammel.scalablockchain.blockchain.Transaction

class Broker extends Actor with ActorLogging {

  //TODO review default impl
  //TODO does this need to be mutable?
  var pending: List[Transaction] = Nil

  override def receive: Receive = {
    case AddTransactionToPending(transaction) =>
      pending = transaction :: pending
      log.info(s"Added $transaction to pending Transaction")
    case GetPendingTransactions =>
      log.info(s"Getting pending transactions")
      sender() ! pending
    //    case DiffTransaction(externalTransactions) => {
//      pending = pending diff externalTransactions
//    }
    case ClearPendingTransactions =>
      pending = Nil
      log.info("Cleared pending transaction List")
//    case SubscribeAck(Subscribe("transaction", None, `self`)) =>
//      log.info("subscribing")
  }
}

object Broker {
  sealed trait BrokerMessage
  case class AddTransactionToPending(transaction: Transaction) extends BrokerMessage
  case object GetPendingTransactions                           extends BrokerMessage
  case object ClearPendingTransactions                         extends BrokerMessage

  val props: Props = Props(new Broker)
}