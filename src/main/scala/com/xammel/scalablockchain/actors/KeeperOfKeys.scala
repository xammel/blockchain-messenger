package com.xammel.scalablockchain.actors

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import com.xammel.scalablockchain.crypto.Crypto
import com.xammel.scalablockchain.crypto.Crypto.base64Encode
import com.xammel.scalablockchain.models.{ActorName, Message, ScalaBlockchainActor}
import com.xammel.scalablockchain.pubsub.PubSub.publishGetPublicKey

import java.security.KeyPair
import scala.concurrent.ExecutionContext.Implicits.global

class KeeperOfKeys(nodeId: String, mediator: ActorRef) extends ScalaBlockchainActor[KeeperOfKeys.KeeperMessage] {

  import KeeperOfKeys._

  lazy val keyPair: KeyPair = Crypto.generateKeyPair

  log.info(s"key pair = $keyPair")

  /*
  case GetRecipientPublicKey(recipientNodeId) if recipientNodeId == nodeId => {
    log.info(s" $nodeId , inside recipient receive fn")
    val keyPair = Crypto.generateKeyPair(nodeId)
    sender() ! Crypto.base64Encode(keyPair.getPublic.getEncoded)
  }
  case GetRecipientPublicKey(recipientNodeId) if recipientNodeId != nodeId => //ignore
   */
  /*
  (mediator ? publishGetPublicKey(GetRecipientPublicKey("node1")))
   */
  override def handleMessages: ReceiveType[KeeperOfKeys.KeeperMessage] = {
    case GetPublicKey(message: Message) => {
      val node = sender()
      (mediator ? publishGetPublicKey(GetRecipientPublicKey(message))).mapTo[String] givenSuccess { key =>
        node ! key
      }
    }
    case GetRecipientPublicKey(message: Message) if message.beneficiary != nodeId => //ignore
    case GetRecipientPublicKey(message: Message) if message.beneficiary == nodeId => sender() ! base64Encode(keyPair.getPublic.getEncoded)
  }
}

object KeeperOfKeys extends ActorName {
  sealed trait KeeperMessage

  case class GetPublicKey(message: Message)          extends KeeperMessage
  case class GetRecipientPublicKey(message: Message) extends KeeperMessage

  def props(nodeId: String, mediator: ActorRef): Props = Props(new KeeperOfKeys(nodeId, mediator))

}
