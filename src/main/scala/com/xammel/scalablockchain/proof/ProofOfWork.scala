package com.xammel.scalablockchain.proof
import com.xammel.scalablockchain.crypto.Crypto.sha256Hash

object ProofOfWork {

  def proofOfWork(hash: String): Long = {
    @scala.annotation.tailrec
    def _proofOfWork(proof: Long): Long = if (isValidProof(hash, proof)) proof else _proofOfWork(proof + 1)
    _proofOfWork(0)
  }

  //TODO make 4 a configurable N ?
  def isValidProof(hash: String, proof: Long) = {

    val concatenated = hash + proof.toString
    val hashed = sha256Hash(concatenated)

    hashed.take(5).forall(_ == '0')
  }

}
