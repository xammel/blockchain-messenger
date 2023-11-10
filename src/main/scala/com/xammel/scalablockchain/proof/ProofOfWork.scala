package com.xammel.scalablockchain.proof
import com.xammel.scalablockchain.crypto.Crypto.sha256Hash

object ProofOfWork {

  /*
  Average times for the proofOfWork algorithm to complete, looking for a hash with N leading 0s
  (over 10 tries, with changing random input hashes)

  scala> averageTime(1)
  res16: Double = 0.0007

  scala> averageTime(2)
  res17: Double = 0.0112

  scala> averageTime(3)
  res18: Double = 0.0967

  scala> averageTime(4)
  res19: Double = 3.1353

  scala> averageTime(5)
  res20: Double = 22.5968
   */
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
