package com.xammel.scalablockchain.models

import akka.actor.{Actor, ActorLogging}
abstract class ScalaBlockchainActor[T: Manifest] extends Actor with ActorLogging {

  type ReceiveType[T] = PartialFunction[T, Unit]
  def handleMessages: ReceiveType[T]

  private def appliedHandleMessages: PartialFunction[Any, Unit] = {case x: T => handleMessages(x)}

  def untypedMessages: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]

  override def receive: PartialFunction[Any, Unit] = appliedHandleMessages orElse untypedMessages

}
