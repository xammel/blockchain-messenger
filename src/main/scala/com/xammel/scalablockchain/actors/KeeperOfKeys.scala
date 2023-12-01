package com.xammel.scalablockchain.actors

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import com.xammel.scalablockchain.crypto.Crypto
import com.xammel.scalablockchain.crypto.Crypto.decrypt
import com.xammel.scalablockchain.models.{ActorName, Message, ScalaBlockchainActor}
import com.xammel.scalablockchain.pubsub.PubSub.publishGetPublicKey

import java.security.{KeyPair, PublicKey}
import scala.concurrent.ExecutionContext.Implicits.global

class KeeperOfKeys(nodeId: String, mediator: ActorRef) extends ScalaBlockchainActor[KeeperOfKeys.KeeperMessage] {

  import KeeperOfKeys._

  lazy val keyPair: KeyPair = Crypto.generateKeyPair
  lazy val privateKey       = keyPair.getPrivate
  lazy val publicKey        = keyPair.getPublic

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
    case GetPublicKey(message: Message) =>
      val node = sender()
      (mediator ? publishGetPublicKey(GetRecipientPublicKey(message))).mapTo[PublicKey] givenSuccess { key =>
        node ! key
      }
    case GetRecipientPublicKey(message) if message.beneficiary != nodeId => //ignore
    case GetRecipientPublicKey(message) if message.beneficiary == nodeId => sender() ! publicKey
    case ReadMessages(messages) => {
      val node = sender()
      val decrypter: String => String     = decrypt(privateKey)
      val decryptedMessages: List[String] = messages.map(_.message).map(decrypter(_))
      println(decryptedMessages)
      node ! decryptedMessages
    }
  }
}

object KeeperOfKeys extends ActorName {
  sealed trait KeeperMessage

  case class GetPublicKey(message: Message)          extends KeeperMessage
  case class GetRecipientPublicKey(message: Message) extends KeeperMessage
  case class ReadMessages(messages: List[Message])    extends KeeperMessage

  def props(nodeId: String, mediator: ActorRef): Props = Props(new KeeperOfKeys(nodeId, mediator))

}
