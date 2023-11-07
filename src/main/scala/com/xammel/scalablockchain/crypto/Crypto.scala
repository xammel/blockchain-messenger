package com.xammel.scalablockchain.crypto

import java.math.BigInteger
import java.security.MessageDigest

object Crypto {

  //TODO review this implementation
  //val encoder: MessageDigest = MessageDigest.getInstance(sha256)
  //    val codeVerifierBytes: Array[Byte] = codeVerifier.getBytes(StandardCharsets.UTF_8)
  //    val data: Array[Byte] = encoder.digest(codeVerifierBytes)
  //?
  def sha256Hash(value: String) = String.format("%064x",
    new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))))

}
