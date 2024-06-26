package com.xammel.scalablockchain.models

import akka.actor.{Actor, ActorLogging, ActorRef, Status}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class ScalaBlockchainActor[T: Manifest] extends Actor with ActorLogging {

  implicit lazy val timeout = Timeout(5.seconds)

  type ReceiveType[T] = PartialFunction[T, Unit]
  def handleMessages: ReceiveType[T]

  private def appliedHandleMessages: Receive = { case x: T => handleMessages(x) }

  private def handleFailure: Receive = { case Status.Failure(e) => throw e }

  def untypedMessages: Receive = PartialFunction.empty[Any, Unit]

  override def receive: Receive = handleFailure orElse appliedHandleMessages orElse untypedMessages
  implicit class FutureHelpers[A](future: Future[A]) {

    def givenSuccess[B](func: A => B)(implicit ec: ExecutionContext): Unit = {
      future onComplete {
        case Success(v) => func(v)
        case Failure(e) => log.error(s"Future completed with a failure: ${e.getMessage}")
      }
    }
  }

  implicit class ActorHelpers(actor: ActorRef) {
    def askAndMap[A: Manifest, B](message: Any)(func: A => B)(implicit timeout: Timeout, ec: ExecutionContext): Unit = {
      (actor ? message).mapTo[A].givenSuccess(func)
    }
  }

}
