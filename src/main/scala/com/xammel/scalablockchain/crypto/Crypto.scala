package com.xammel.scalablockchain.crypto

import java.nio.charset.StandardCharsets
import java.security._
import java.util.Base64
import javax.crypto.Cipher

object Crypto {

  lazy val RSA = "RSA"
  lazy val sha256 = "SHA-256"

  def sha256Hash(value: String) = {
    val encoder: MessageDigest    = MessageDigest.getInstance(sha256)
    val valueBytes: Array[Byte]   = value.getBytes(StandardCharsets.UTF_8)
    val encodedBytes: Array[Byte] = encoder.digest(valueBytes)
    val charArray: Array[String]  = encodedBytes.map("%02x".format(_))
    charArray.mkString
  }

  def base64Encode(bytes: Array[Byte]): String = {
    Base64.getEncoder.encodeToString(bytes)
  }

  def base64Decode(string: String): Array[Byte] = {
    Base64.getDecoder.decode(string)
  }

  def encrypt(publicKey: PublicKey)(message: String) = {
    val cipher: Cipher = Cipher.getInstance(RSA)
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val data: Array[Byte] = cipher.doFinal(message.getBytes)
    base64Encode(data)
  }

  def decrypt(privateKey: PrivateKey)(encryptedMessage: String) = {
    val data           = base64Decode(encryptedMessage)
    val cipher: Cipher = Cipher.getInstance(RSA)
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    new String(cipher.doFinal(data))
  }

  def generateKeyPair = {
    val gen = KeyPairGenerator.getInstance(RSA)
    gen.initialize(1024, new SecureRandom)
    gen.generateKeyPair
  }

//  val nodeId                                            = "node0"
//  val keyPair                                           = generateKeyPair
//  val Seq(privateKey: PrivateKey, publicKey: PublicKey) = Seq(keyPair.getPrivate, keyPair.getPublic)
//
//  val message          = "hi there my name is mindy"
//  val encryptedMessage = encrypt(publicKey)(message)
//  val decryptedMessage2 = decrypt(privateKey)(encryptedMessage)
//
//  val keyPair2 = generateKeyPair
//  val Seq(privateKey2: PrivateKey, publicKey2: PublicKey) =
//    Seq(keyPair2.getPrivate, keyPair2.getPublic)
//
//  //val decryptedMessage2 = decrypt(privateKey2)(encryptedMessage)
//  val encrypted2 = encrypt(publicKey2)(message)
//  val decrypted2 = decrypt(privateKey2)(encrypted2)

}
