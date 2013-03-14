package paypal.controllers

import play.api.mvc.RequestHeader
import play.api.Logger

//object PaypalTransaction {
//  def findByTxnId(id: String): PaypalTransaction
//}

/** Extend this class for a concrete implementation that can be persisted */
abstract class PaypalTransaction(dataMap: Map[String, Seq[String]]) {
  val txnId: String
  val receiverEmail: String
}

abstract class TransactionProcessor(txn: String, customerAddress: CustomerAddress)(implicit request: RequestHeader) {
  def processTransaction: Unit

  def handleDuplicateTransaction(txn: PaypalTransaction): Unit =
    Logger.info("Ignoring duplicate transaction: " + txn.toString)
}

abstract class CustomerAddress(dataMap: Map[String, Seq[String]]) {
}
