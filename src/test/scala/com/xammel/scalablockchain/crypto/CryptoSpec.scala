package com.xammel.scalablockchain.crypto

import com.xammel.scalablockchain.crypto.Crypto.sha256Hash
import org.scalatest.flatspec._
import org.scalatest.matchers._

class CryptoSpec extends AnyFlatSpec with should.Matchers {

  lazy val testString     = "testString"
  lazy val expectedResult = "4acf0b39d9c4766709a3689f553ac01ab550545ffa4544dfc0b2cea82fba02a3"

  "sha256Hash" should "hash the input with the SHA-256 algorithm" in {
    sha256Hash(testString) shouldEqual expectedResult
  }

}
