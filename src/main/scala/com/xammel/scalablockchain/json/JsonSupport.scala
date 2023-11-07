package com.xammel.scalablockchain.json

import com.xammel.scalablockchain.blockchain._
import spray.json._

object JsonSupport extends DefaultJsonProtocol {

  implicit object TransactionJsonFormat extends RootJsonFormat[Transaction] {
    def write(transaction: Transaction): JsValue = JsObject(
      "sender"    -> JsString(transaction.originator),
      "recipient" -> JsString(transaction.beneficiary),
      "value"     -> JsNumber(transaction.value)
    )

    def read(value: JsValue): Transaction = {
      value.asJsObject.getFields("originator", "beneficiary", "value") match {
        case Seq(JsString(sender), JsString(recipient), JsNumber(amount)) =>
          Transaction(sender, recipient, amount.toLong)
        case _ => throw DeserializationException("Transaction expected")
      }
    }
  }

  implicit object PopulatedBlockFormat extends RootJsonFormat[PopulatedBlock] {
    override def write(populatedBlock: PopulatedBlock): JsValue = JsObject(
      "index"        -> JsNumber(populatedBlock.index),
      "transactions" -> JsArray(populatedBlock.transactions.map(_.toJson).toVector),
      "proof"        -> JsNumber(populatedBlock.proof),
      "timestamp"    -> JsNumber(populatedBlock.timestamp)
    )

    override def read(json: JsValue): PopulatedBlock = {
      json.asJsObject.getFields(
        "index",
        "transactions",
        "proof",
        "timestamp"
      ) match {
        case Seq(
              JsNumber(index),
              transactions,
              JsNumber(proof),
              JsNumber(timestamp)
            ) =>
          PopulatedBlock(
            index = index.toInt,
            transactions = transactions.convertTo[List[Transaction]],
            proof = proof.toLong,
            timestamp = timestamp.toLong
          )

      }
    }
  }

  implicit object BlockJsonFormat extends RootJsonFormat[Block] {
    override def read(json: JsValue): Block = json.asJsObject.getFields("transactions") match {
      case Seq(_) => json.convertTo[PopulatedBlock]
      case Seq()  => GenesisBlock
      case _      => throw DeserializationException("Cannot deserialize: Block expected")
    }

    override def write(block: Block): JsValue = block match {
      case populatedBlock: PopulatedBlock => PopulatedBlockFormat.write(populatedBlock)
      case GenesisBlock =>
        JsObject(
          "index"     -> JsNumber(GenesisBlock.index),
          "hash"      -> JsString(GenesisBlock.hash),
          "timestamp" -> JsNumber(GenesisBlock.timestamp)
        )
    }
  }

  implicit object NonEmptyChainJsonFormat extends RootJsonFormat[NonEmptyChain] {
    override def write(obj: NonEmptyChain): JsValue = JsObject(
      "blocks" -> JsArray(obj.blocks.map(_.toJson).toVector)
    )

    override def read(json: JsValue): NonEmptyChain = json.asJsObject.getFields("blocks") match {
      case Seq(blocks) => NonEmptyChain(blocks.convertTo[List[Block]])
    }
  }

  implicit object ChainJsonFormat extends RootJsonFormat[Chain] {
    def write(obj: Chain): JsValue = obj match {
      case nonEmptyChain: NonEmptyChain => nonEmptyChain.toJson
      case EmptyChain =>
        JsObject(
          "blocks" -> JsArray(obj.blocks.map(_.toJson).toVector)
        )
    }

    def read(json: JsValue): Chain = {
      json.asJsObject.getFields("blocks") match {
        case Seq(blocks) => {
          val blockList = blocks.convertTo[List[Block]]
          //TODO I think this will initialize a new EmptyChain and therefore create a new timestamp...
          if (blockList.length <= 1) EmptyChain
          else json.convertTo[NonEmptyChain]
        }
      }
    }
  }

}
