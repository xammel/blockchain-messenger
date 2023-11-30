package com.xammel.scalablockchain.actors

import akka.actor.{ActorRef, Props, Status}
import akka.pattern.ask
import com.xammel.scalablockchain.crypto.Crypto
import com.xammel.scalablockchain.models.{ActorName, Message, ScalaBlockchainActor}
import com.xammel.scalablockchain.pubsub.PubSub.publishGetPublicKey
import scala.concurrent.ExecutionContext.Implicits.global

import java.security.KeyPair
import scala.util.{Failure, Success}

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
      //TODO revert
      log.info(s"publishing to ${message.beneficiary} - $nodeId")
      val node = sender()
      (mediator ? publishGetPublicKey(GetRecipientPublicKey(message))).mapTo[String] onComplete  {
        case Success(key) => node ! key
        case Failure(e) => node ! Status.Failure(e)
      }
    }
    case GetRecipientPublicKey(message: Message) if message.beneficiary != nodeId => //ignore
    case GetRecipientPublicKey(message: Message) if message.beneficiary == nodeId =>
      //TODO revert
      log.info(s"inside the sending func to ${message.beneficiary} - $nodeId")
      sender() ! Crypto.base64Encode(keyPair.getPublic.getEncoded)
  }
}

object KeeperOfKeys extends ActorName {
  sealed trait KeeperMessage

  case class GetPublicKey(message: Message)          extends KeeperMessage
  case class GetRecipientPublicKey(message: Message) extends KeeperMessage

  def props(nodeId: String, mediator: ActorRef): Props = Props(new KeeperOfKeys(nodeId, mediator))

}
