package com.xammel.scalablockchain.actors

import akka.actor.Props
import akka.actor.Status.{Failure, Success}
import com.xammel.scalablockchain.exceptions.{InvalidProofException, MinerBusyException}
import com.xammel.scalablockchain.models.{ActorName, ScalaBlockchainActor}
import com.xammel.scalablockchain.proof.ProofOfWork._

import scala.concurrent.Future
class Miner extends ScalaBlockchainActor[Miner.MinerMessage] {

  import Miner._
  import context._

  override def handleMessages: ReceiveType[MinerMessage] = { case ReadyYourself =>
    become(ready)
  }

  private def ready: Receive = validate orElse {
    case Mine(hash) =>
      log.info(s"Mining hash $hash...")
      val proof: Future[Long] = Future {
        proofOfWork(hash)
      }
      sender() ! proof
      become(busy)
    case ReadyYourself =>
      log.info("I'm ready to mine!")
      sender() ! Success("OK")
  }

  private def validate: Receive = { case Validate(hash, proof) =>
    log.info(s"Validating proof $proof")
    if (isValidProof(hash, proof)) {
      log.info("Proof is valid!")
      sender() ! Success
    } else {
      log.info("Proof is not valid")
      sender() ! Failure(InvalidProofException(hash, proof))
    }
  }

  private def busy: Receive = validate orElse {
    case Mine(_) =>
      log.info("I'm already mining")
      sender ! Failure(MinerBusyException)
    case ReadyYourself =>
      log.info("Ready to mine a new block")
      become(ready)
  }
}

object Miner extends ActorName {
  sealed trait MinerMessage
  case class Validate(hash: String, proof: Long) extends MinerMessage
  case class Mine(hash: String)                  extends MinerMessage
  case object ReadyYourself                      extends MinerMessage

  val props: Props = Props(new Miner)
}
