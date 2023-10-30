package com.xammel.scalablockchain.proof
import com.xammel.scalablockchain.crypto.Crypto.sha256Hash

object ProofOfWork {

  def proofOfWork(hash: String): Long = {
    @scala.annotation.tailrec
    def _proofOfWork(proof: Long): Long = if (isValid(hash, proof)) proof else _proofOfWork(proof + 1)
    _proofOfWork(0)
  }

  //TODO make 4 a configurable N ?
  def isValid(hash: String, proof: Long) = {
    //TODO beware, in the github impl this is (concatted).asJson which adds quote marks around it...
    val concat = hash + proof.toString
    val hashed = sha256Hash(concat)

    hashed.take(4).forall(_ == '0')
  }

}
