package com.xammel.scalablockchain.blockchain

import com.xammel.scalablockchain.crypto.Crypto.sha256Hash
import com.xammel.scalablockchain.utils.ErrorMessages.emptyChainAdditionMessage
import io.circe.generic.auto._
import io.circe.syntax._

import java.security.InvalidParameterException

//TODO, should Chain be Block, and then also have another Blockchain data structure that takes a Seq[Block] ?

sealed trait Chain {
  //TODO I think this is actually latestIndex in the chain?
  val index: Int
  //TODO I think this is similarly latestHash in chain
  val previousHash: String
  val transactions: List[Transaction]
  val proof: Long
  val timestamp: Long

  //TODO pretty sure this doesn't work as intended, think tail should concatenate rather than overwrite
  def ::(link: Chain): Chain = link match {
    case link: ChainLink => link.copy(previousHash = this.previousHash, tail = this)
    case EmptyChain      => throw new InvalidParameterException(emptyChainAdditionMessage)
  }
}

object Chain {
  def apply(chainLinks: Seq[Chain]): Chain = chainLinks match {
    case Nil => EmptyChain
    case (link: ChainLink) :: tail =>
      ChainLink(link.index, link.proof, link.transactions, link.previousHash, apply(tail))
  }
}

case class ChainLink(
    index: Int,
    proof: Long,
    transactions: List[Transaction],
    previousHash: String = "",
    tail: Chain = EmptyChain,
    timestamp: Long = System.currentTimeMillis()
) extends Chain {
  val hash = sha256Hash(this.asJson.noSpaces)
}

//TODO review these defaults
case object EmptyChain extends Chain {
  override val index: Int                      = 0
  override val previousHash: String            = "1"
  override val transactions: List[Transaction] = List.empty
  override val proof: Long                     = 100
  override val timestamp: Long                 = System.currentTimeMillis()
}
