package com.xammel.scalablockchain.pubsub

import akka.actor.ActorRef
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}
import com.xammel.scalablockchain.actors.{KeeperOfKeys, Node}

object PubSub {

  val newBlock     = "newBlock"
  val transaction  = "transaction"
  val getPublicKey = "publicKey"

  def subscribeNewBlock(actor: ActorRef)       = Subscribe(newBlock, actor)
  def publishNewBlock(addBlock: Node.AddBlock) = Publish(newBlock, addBlock)
  def subscribeTransaction(actor: ActorRef)    = Subscribe(transaction, actor)

  def publishTransaction(transactionMessage: Node.TransactionMessage) =
    Publish(transaction, transactionMessage)

  def subscribeGetPublicKey(actor: ActorRef)                     = Subscribe(getPublicKey, actor)
  def publishGetPublicKey(get: KeeperOfKeys.GetPublicKeyMessage) = Publish(getPublicKey, get)

}
