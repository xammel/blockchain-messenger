package com.xammel.scalablockchain.proof

import com.xammel.scalablockchain.proof.ProofOfWork.isValidProof
import org.scalatest.flatspec._
import org.scalatest.matchers._

class ProofOfWorkSpec extends AnyFlatSpec with should.Matchers {

  lazy val testHash           = "testHash"
  lazy val validProofOfWork   = 55461
  lazy val invalidProofOfWork = 55460

  "isValid" should "identify a valid proof of work" in {
    isValidProof(testHash, validProofOfWork) shouldBe true
  }

  "isValid" should "return false for an invalid proof of work" in {
    isValidProof(testHash, invalidProofOfWork) shouldBe false
  }

}
