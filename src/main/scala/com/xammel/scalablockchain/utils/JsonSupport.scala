package com.xammel.scalablockchain.utils

import com.xammel.scalablockchain.blockchain.{Chain, ChainLink, EmptyChain, Transaction}
import spray.json._

object JsonSupport extends DefaultJsonProtocol {

  implicit object TransactionJsonFormat extends RootJsonFormat[Transaction] {
    def write(t: Transaction) = JsObject(
      "sender"    -> JsString(t.originator),
      "recipient" -> JsString(t.beneficiary),
      "value"     -> JsNumber(t.value)
    )

    def read(value: JsValue) = {
      value.asJsObject.getFields("originator", "beneficiary", "value") match {
        case Seq(JsString(sender), JsString(recipient), JsNumber(amount)) =>
          Transaction(sender, recipient, amount.toLong)
        case _ => throw DeserializationException("Transaction expected")
      }
    }
  }

  implicit object ChainLinkJsonFormat extends RootJsonFormat[ChainLink] {
    override def read(json: JsValue): ChainLink = json.asJsObject.getFields(
      "index",
      "proof",
      "transactions",
      "previousHash",
      "tail",
      "timestamp"
    ) match {
      case Seq(
            JsNumber(index),
            JsNumber(proof),
            values,
            JsString(previousHash),
            tail,
            JsNumber(timestamp)
          ) =>
        ChainLink(
          index = index.toInt,
          proof = proof.toLong,
          transactions = values.convertTo[List[Transaction]],
          previousHash = previousHash,
          tail = tail.convertTo(ChainJsonFormat),
          timestamp = timestamp.toLong
        )
      case _ => throw DeserializationException("Cannot deserialize: Chainlink expected")
    }

    override def write(obj: ChainLink): JsValue = JsObject(
      "index"        -> JsNumber(obj.index),
      "proof"        -> JsNumber(obj.proof),
      "values"       -> JsArray(obj.transactions.map(_.toJson).toVector),
      "previousHash" -> JsString(obj.previousHash),
      "timestamp"    -> JsNumber(obj.timestamp),
      "tail"         -> ChainJsonFormat.write(obj.tail)
    )
  }

  implicit object ChainJsonFormat extends RootJsonFormat[Chain] {
    def write(obj: Chain): JsValue = obj match {
      case link: ChainLink => link.toJson
      case EmptyChain =>
        JsObject(
          "index"        -> JsNumber(EmptyChain.index),
          "hash"         -> JsString(EmptyChain.previousHash),
          "transactions" -> JsArray(),
          "proof"        -> JsNumber(EmptyChain.proof),
          "timeStamp"    -> JsNumber(EmptyChain.timestamp)
        )
    }

    def read(json: JsValue): Chain = {
      json.asJsObject.getFields("previousHash") match {
        case Seq(_) => json.convertTo[ChainLink]
        case Seq()  => EmptyChain
      }
    }
  }

}
