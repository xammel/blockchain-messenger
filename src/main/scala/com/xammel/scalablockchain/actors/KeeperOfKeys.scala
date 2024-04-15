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

  private lazy val keyPair: KeyPair            = Crypto.generateKeyPair
  private lazy val privateKey: PrivateKey      = keyPair.getPrivate
  private lazy val publicKey: PublicKey        = keyPair.getPublic
  private lazy val decryptor: String => String = decrypt(privateKey)

  override def handleMessages: ReceiveType[KeeperOfKeys.KeeperMessage] = {
    case EncryptMessage(messageTxn) =>
      val senderRef = sender
      self.askAndMap(GetRecipientPublicKey(messageTxn)) { publicKey: PublicKey =>
        val encryptor: String => String = encrypt(publicKey)
        senderRef ! messageTxn.copy(message = encryptor(messageTxn.message))
      }
    case GetRecipientPublicKey(messageTxn) =>
      val senderRef = sender
      val publish   = publishGetPublicKey(GetPublicKeyMessage(messageTxn))
      mediator.askAndMap(publish) { key => senderRef ! key }
    case GetPublicKeyMessage(messageTxn) if messageTxn.beneficiary != nodeId => log.info(s"Not sending my public key")
    case GetPublicKeyMessage(messageTxn) if messageTxn.beneficiary == nodeId => {
      log.info(s"Sending public key to $sender")
      sender ! publicKey
    }
    case ReadMessages(messageTxns) =>
      val decryptedMessages: List[MessageTransaction] = messageTxns.map { messageTxn =>
        println("messagetxn message:", messageTxn.message)
        messageTxn.copy(message = decryptor(messageTxn.message))
      }
      println("done decrypting")
      sender ! decryptedMessages
  }

}

object KeeperOfKeys extends ActorName {
  sealed trait KeeperMessage

  case class EncryptMessage(messageTransaction: MessageTransaction)                extends KeeperMessage
  case class GetPublicKeyMessage(message: MessageTransaction)                      extends KeeperMessage
  case class ReadMessages(messageTransactions: List[MessageTransaction])           extends KeeperMessage
  private case class GetRecipientPublicKey(messageTransaction: MessageTransaction) extends KeeperMessage

  def props(nodeId: String, mediator: ActorRef): Props = Props(new KeeperOfKeys(nodeId, mediator))

}
