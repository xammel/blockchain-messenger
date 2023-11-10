package com.xammel.scalablockchain.crypto

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object Crypto {
  def sha256Hash(value: String) = {
    val encoder: MessageDigest    = MessageDigest.getInstance("SHA-256")
    val valueBytes: Array[Byte]   = value.getBytes(StandardCharsets.UTF_8)
    val encodedBytes: Array[Byte] = encoder.digest(valueBytes)
    val charArray: Array[String]  = encodedBytes.map("%02x".format(_))
    charArray.mkString
  }

}
