package paypal

import play.api.mvc.RequestHeader
import play.api.Logger

/** Extend this trait for a concrete implementation that can be persisted */
trait PaypalTransaction {
  def apply(dataMap: Option[Map[String, Seq[String]]]): PaypalTransaction

  def findByTxnId(id: String): PaypalTransaction

  val txnId: String

  val receiverEmail: String
}

trait TransactionProcessor {
  def createFrom(txn: String, customerAddress: CustomerAddress)(implicit request: RequestHeader)

  def processTransaction: Unit

  def handleDuplicateTransaction(txn: PaypalTransaction): Unit =
    Logger.info("Ignoring duplicate transaction: " + txn.toString)
}

trait CustomerAddress {
  def createFrom(dataMap: Option[Map[String, Seq[String]]]): CustomerAddress
}
