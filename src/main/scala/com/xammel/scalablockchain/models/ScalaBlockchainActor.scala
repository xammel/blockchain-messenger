package com.xammel.scalablockchain.models

import akka.actor.{Actor, ActorLogging, ActorRef, Status}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

abstract class ScalaBlockchainActor[T: Manifest] extends Actor with ActorLogging {

  implicit lazy val timeout = Timeout(5.seconds)

  type ReceiveType[T] = PartialFunction[T, Unit]
  def handleMessages: ReceiveType[T]

  private def appliedHandleMessages: PartialFunction[Any, Unit] = { case x: T => handleMessages(x) }

  def untypedMessages: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]

  override def receive: PartialFunction[Any, Unit] = appliedHandleMessages orElse untypedMessages
  implicit class FutureHelpers[A](future: Future[A]) {
    def givenSuccess[B](func: A => B) = {
      future onComplete {
        case Success(v) => func(v)
        case Failure(e) => sender() ! Status.Failure(e)
      }
    }
  }
  //TODO implement?
  //  implicit class ActorHelpers(actor: ActorRef) {
//    def askAndMap[A: Manifest, B](message: Any)(func: A => B)(implicit timeout: Timeout): Unit = {
//      (actor ? message).mapTo[A].givenSuccess(func)
//    }
//  }

}
