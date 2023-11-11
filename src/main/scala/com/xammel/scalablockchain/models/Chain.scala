package com.xammel.scalablockchain.models

import com.xammel.scalablockchain.crypto.Crypto
import com.xammel.scalablockchain.crypto.Crypto.sha256Hash

import scala.util.Random

 sealed trait Chain {
  val blocks: List[Block]
  def numberOfBlocks                 = blocks.length
  private def mostRecentBlock: Block = blocks.maxBy(_.timestamp) //TODO unsafe accessor
  def mostRecentBlocksIndex: Long    = mostRecentBlock.index
  def mostRecentBlocksHash: String   = mostRecentBlock.hash

  def addBlock(transactions: List[Transaction], proof: Long): NonEmptyChain =
    NonEmptyChain(blocks = this.blocks :+ createNextBlock(transactions, proof))

  def createNextBlock(transactions: List[Transaction], proof: Long): PopulatedBlock = PopulatedBlock(
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
  val hash = sha256Hash(this.toString)
}

case object GenesisBlock extends Block {
  override val index: Long     = 0
  override val hash: String    = Crypto.sha256Hash("GenesisBlock")
  override val timestamp: Long = 0
}
