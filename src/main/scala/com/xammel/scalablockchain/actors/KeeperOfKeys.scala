package com.xammel.scalablockchain.actors

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import com.xammel.scalablockchain.crypto.Crypto
import com.xammel.scalablockchain.crypto.Crypto.{decrypt, encrypt}
import com.xammel.scalablockchain.models.{ActorName, Message, ScalaBlockchainActor}
import com.xammel.scalablockchain.pubsub.PubSub.{publishGetPublicKey, subscribeGetPublicKey}

import java.security.{KeyPair, PrivateKey, PublicKey}
import scala.concurrent.ExecutionContext.Implicits.global

class KeeperOfKeys(nodeId: String, mediator: ActorRef) extends ScalaBlockchainActor[KeeperOfKeys.KeeperMessage] {

  import KeeperOfKeys._

  mediator ! subscribeGetPublicKey(self)

  lazy val keyPair: KeyPair                                       = Crypto.generateKeyPair
  private lazy val (privateKey: PrivateKey, publicKey: PublicKey) = (keyPair.getPrivate, keyPair.getPublic)
  private lazy val encryptor: String => String                         = encrypt(publicKey)
  private lazy val decryptor: String => String                         = decrypt(privateKey)

  override def handleMessages: ReceiveType[KeeperOfKeys.KeeperMessage] = {
    case EncryptMessage(msg) => sender() ! msg.copy(message = encryptor(msg.message))
    case GetPublicKey(message) =>
      val senderRef = sender()
      (mediator ? publishGetPublicKey(GetRecipientPublicKey(message))).mapTo[PublicKey] givenSuccess { key =>
        senderRef ! key
      }
    case GetRecipientPublicKey(message) if message.beneficiary != nodeId => //ignore
    case GetRecipientPublicKey(message) if message.beneficiary == nodeId => sender() ! publicKey
    case ReadMessages(messages) =>
      val decryptedMessages: List[Message] = messages.map(msg => msg.copy(message = decryptor(msg.message)))
      sender() ! decryptedMessages
  }
}

object KeeperOfKeys extends ActorName {
  sealed trait KeeperMessage

  case class EncryptMessage(message: Message)        extends KeeperMessage
  case class GetRecipientPublicKey(message: Message) extends KeeperMessage
  case class ReadMessages(messages: List[Message])   extends KeeperMessage
  private case class GetPublicKey(message: Message)  extends KeeperMessage

  def props(nodeId: String, mediator: ActorRef): Props = Props(new KeeperOfKeys(nodeId, mediator))

}
