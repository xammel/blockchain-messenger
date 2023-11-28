package com.xammel.scalablockchain.exceptions

import spray.json.JsValue

case class InvalidProofException(hash: String, proof: Long) extends Exception(s"The proof $proof is not valid for the hash $hash")

case object MinerBusyException extends Exception("Miner is busy")

case class DeserializationError(fieldsRecieved: Seq[JsValue], fieldsExpected: Seq[String]) extends Exception {

  val expectedFields              = fieldsExpected.mkString("[", ", ", "]")
  val receivedFields              = fieldsRecieved.map(_.toString).mkString(", ")
  override def getMessage: String = s"Expected fields $expectedFields but received values $receivedFields"
}
