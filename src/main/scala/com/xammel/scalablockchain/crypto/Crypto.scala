package com.xammel.scalablockchain.crypto

import java.math.BigInteger
import java.security.MessageDigest

object Crypto {

  //TODO review this implementation
  def sha256Hash(value: String) = String.format("%064x",
    new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))))

}
