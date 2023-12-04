package com.xammel.scalablockchain.models

import com.xammel.scalablockchain.crypto.Crypto
import com.xammel.scalablockchain.crypto.Crypto.sha256Hash

sealed trait Chain {
  val blocks: List[Block]

  private def mostRecentBlock: Block = blocks.maxBy(_.timestamp) //TODO unsafe accessor
  def mostRecentBlocksIndex: Long    = mostRecentBlock.index
  def mostRecentBlocksHash: String   = mostRecentBlock.hash

  def addBlock(transactions: List[Transaction], proof: Long, timestamp: Long): NonEmptyChain = {
    val newBlock = PopulatedBlock(this.mostRecentBlocksIndex + 1, transactions, proof, timestamp)
    NonEmptyChain(blocks = this.blocks :+ newBlock)
  }

  private def populatedBlocks: List[PopulatedBlock] = blocks.collect { case p: PopulatedBlock => p }

  def allTransactions: List[Transaction] = populatedBlocks.flatMap(_.transactions)
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
  override val hash: String    = Crypto.sha256Hash(this.getClass.getName)
  override val timestamp: Long = 0
}
