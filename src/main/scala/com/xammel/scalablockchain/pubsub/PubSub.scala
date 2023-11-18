package com.xammel.scalablockchain.pubsub

import akka.actor.ActorRef
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}
import com.xammel.scalablockchain.actors.Node

object PubSub {

  val newBlock    = "newBlock"
  val transaction = "transaction"

  def subscribeNewBlock(actor: ActorRef)       = Subscribe(newBlock, actor)
  def subscribeTransaction(actor: ActorRef)    = Subscribe(transaction, actor)
  def publishNewBlock(addBlock: Node.AddBlock) = Publish(newBlock, addBlock)
  def publishTransaction(transactionMessage: Node.TransactionMessage) =
    Publish(transaction, transactionMessage)

}
