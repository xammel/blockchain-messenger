package com.xammel.scalablockchain.exceptions

case class InvalidProofException(hash: String, proof: Long) extends Exception(s"The proof $proof is not valid for the hash $hash")

case object MinerBusyException extends Exception("Miner is busy")
