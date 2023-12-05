package com.xammel.scalablockchain.actors

import akka.actor.{ActorRef, Props}
import com.xammel.scalablockchain.crypto.Crypto
import com.xammel.scalablockchain.crypto.Crypto.{decrypt, encrypt}
import com.xammel.scalablockchain.models.{ActorName, MessageTransaction, ScalaBlockchainActor}
import com.xammel.scalablockchain.pubsub.PubSub.{publishGetPublicKey, subscribeGetPublicKey}

import java.security.{KeyPair, PrivateKey, PublicKey}
import scala.concurrent.ExecutionContext.Implicits.global

class KeeperOfKeys(nodeId: String, mediator: ActorRef) extends ScalaBlockchainActor[KeeperOfKeys.KeeperMessage] {

  import KeeperOfKeys._

  mediator ! subscribeGetPublicKey(self)

  lazy val keyPair: KeyPair                                       = Crypto.generateKeyPair
  private lazy val (privateKey: PrivateKey, publicKey: PublicKey) = (keyPair.getPrivate, keyPair.getPublic)
  //TODO currently using host node to encrypt the message, not the recipient public key!
  private lazy val encryptor: String => String                    = encrypt(publicKey)
  private lazy val decryptor: String => String                    = decrypt(privateKey)

  override def handleMessages: ReceiveType[KeeperOfKeys.KeeperMessage] = {
    case EncryptMessage(messageTxn) => sender ! messageTxn.copy(message = encryptor(messageTxn.message))
    case GetPublicKey(messageTxn) =>
      val senderRef = sender
      val publish   = publishGetPublicKey(GetRecipientPublicKey(messageTxn))
      mediator.askAndMap(publish) { key => senderRef ! key }
    case GetRecipientPublicKey(messageTxn) if messageTxn.beneficiary != nodeId => //ignore
    case GetRecipientPublicKey(messageTxn) if messageTxn.beneficiary == nodeId => sender ! publicKey
    case ReadMessages(messageTxns) =>
      val decryptedMessages: List[MessageTransaction] = messageTxns.map { messageTxn =>
        messageTxn.copy(message = decryptor(messageTxn.message))
      }
      sender ! decryptedMessages
  }
}

object KeeperOfKeys extends ActorName {
  sealed trait KeeperMessage

  case class EncryptMessage(messageTransaction: MessageTransaction)       extends KeeperMessage
  case class GetRecipientPublicKey(message: MessageTransaction)           extends KeeperMessage
  case class ReadMessages(messageTransactions: List[MessageTransaction])  extends KeeperMessage
  private case class GetPublicKey(messageTransaction: MessageTransaction) extends KeeperMessage

  def props(nodeId: String, mediator: ActorRef): Props = Props(new KeeperOfKeys(nodeId, mediator))

}
