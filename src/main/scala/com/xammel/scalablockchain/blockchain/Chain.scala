package com.xammel.scalablockchain.blockchain

import com.xammel.scalablockchain.crypto.Crypto
import com.xammel.scalablockchain.crypto.Crypto.sha256Hash
import com.xammel.scalablockchain.json.JsonSupport.PopulatedBlockFormat
import spray.json._
import scala.util.Random

sealed trait Chain {
  val blocks: List[Block]
  def numberOfBlocks               = blocks.length
  def mostRecentBlocksIndex: Long  = blocks.minBy(_.timestamp).index //TODO unsafe accessor
  def mostRecentBlocksHash: String = blocks.minBy(_.timestamp).hash  //TODO unsafe accessor

  def addBlock(transactions: List[Transaction], proof: Long): NonEmptyChain =
    NonEmptyChain(blocks = this.blocks :+ createNextBlock(transactions, proof))

  def createNextBlock(transactions: List[Transaction], proof: Long): PopulatedBlock =
    PopulatedBlock(
      index = this.mostRecentBlocksIndex + 1,
      transactions = transactions,
      proof = proof,
      timestamp = System.currentTimeMillis()
    )
}

case class NonEmptyChain(blocks: List[Block]) extends Chain

case object EmptyChain extends Chain {
  override val blocks: List[Block] = List(GenesisBlock)
}

sealed trait Block {
  val index: Long
  val hash: String
  val timestamp: Long
}

case class PopulatedBlock(
    index: Long,
    transactions: List[Transaction],
    proof: Long,
    timestamp: Long
) extends Block {
  val hash = sha256Hash(this.toJson(PopulatedBlockFormat).toString)
}

case object GenesisBlock extends Block {
  private val randomSeedString = Random.alphanumeric.take(10).toString
  override val index: Long     = 0
  override val hash: String    = Crypto.sha256Hash(randomSeedString)
  override val timestamp: Long = System.currentTimeMillis()
}
