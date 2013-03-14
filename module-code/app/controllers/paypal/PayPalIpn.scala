package paypal

import paypal.{PaypalResponse, PaypalRequest, PaypalBase}
import play.api.mvc.{Action, RequestHeader}
import play.api.mvc.Results.Ok
import concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import concurrent.{Await, Future, ExecutionContext}
import java.net.URLEncoder
import play.api.Logger

/** Handle the incoming request, dispatch the IPN callback, and handle the subsequent response.
  * This is treated like a case class, but because it is really only an object and a companion class, it can be extended.
  * It probably makes more sense to shred this and rewrite */
object PaypalIPN {
  /** @todo Really need to make sure that multiple custom parameters can be mapped through.
    * The current solution is not good! */
  // todo rewrite this recursive lookup so it works with Play
   private def paramsAsPayloadList(request: RequestHeader): Seq[(String, String)] =
    (for (p <- request.body.asFormUrlEncoded; mp <- p._2.map(v => (p._1, v))) yield (mp._1, mp._2)).toList

  def apply(request: RequestHeader, mode: PaypalMode, connection: PaypalConnection) = {
    //create request, get response and pass response object to the specified event handlers
    val ipnResponse: PaypalIPNPostbackReponse = PaypalIPNPostback(mode, connection, paramsAsPayloadList(request))
    ipnResponse
  }
}

trait BasePaypalTrait {

  // todo replace the clunky HttpClient code from LiftWeb with this
  def synchronousPost(url: String, dataMap: Map[String, Seq[String]], timeout: Duration=Duration.create(30, TimeUnit.SECONDS)): String = {
    import play.api.libs.ws.{ WS, Response => WSResponse }
    import ExecutionContext.Implicits.global
    val params = dataMap.map { case (k, v) => "%s=%s".format(k, URLEncoder.encode(v.head, "UTF-8")) }.mkString("&")
    val future: Future[WSResponse] = WS.url(url).withHeaders(("Content-Type", "application/x-www-form-urlencoded")).
       post(params)
    try {
      Await.result(future, timeout)
      future.value.get.get.body // this line should be redone ... ack!
    } catch {
      case ex: Exception =>
        Logger.error(ex.toString)
        ex.toString
    }
  }
}

trait PaypalIPN[PPT <: PaypalTransaction, CA <: CustomerAddress, TP <: TransactionProcessor] extends BasePaypalTrait {
  /** @see [[https://www.paypal.com/cgi-bin/webscr?cmd=p/acc/ipn-info-outside]] */
  // todo decouple this from PaypalTransactions and CustomerAddresses, then replace the LiftWeb IPN code
  def ipn = Action { implicit request =>
    request.body.asFormUrlEncoded match {
      case Some(dataMap) =>
        Logger.info("\nPaypal request: " + dataMap)
        synchronousPost(Rules.url, dataMap + ("cmd" -> List("_notify-validate"))) match {
          case "VERIFIED" =>
            val paymentStatus = dataMap.getOrElse("payment_status", List("")).head
            if (paymentStatus == "Completed") {
              val txn = PPT.createFrom(dataMap)
              PPT.findByTxnId(txn.txnId) match {
                case Some(txn) =>
                  Logger.info("Ignoring duplicate transaction: " + txn.toString)

                case None =>
                  // Validate that the "receiver_email" is an email address registered in our PayPal account
                  if (txn.receiverEmail!=receiverEmail) {
                    Logger.warn("Potential fraud attempt: receiver_email did not match in " + txn.toString)
                  } else {
                    val customerAddress = CA.createFrom(dataMap)
                    TP.createFrom(txn, customerAddress).processTransaction
                  }
              }
            } else { // paymentStatus might be "Pending" or "Failed"
              Logger.warn("Verified but payment_status is " + paymentStatus)
            }

          case response => // most likely response is "INVALID"
            Logger.warn("Could not verify Paypal transaction via POST to %s; verification response: '%s'".format(url, response))
        }

      case None =>
    }
    Ok
  }

}

/**
 * In response to the IPN postback from PayPal, its necessary to then call PayPal and pass back
 * the exact set of parameters that were previously received from PayPal - this stops spoofing. Use the
 * PaypalInstantPaymentTransferPostback exactly as you would PaypalDataTransferResponse.
 */
private[paypal] object PaypalIPNPostback extends PaypalBase {

  def payloadArray(parameters: Seq[(String, String)]) = List("cmd" -> "_notify-validate") ++ parameters

  def apply(mode: PaypalMode, connection: PaypalConnection, parameters: Seq[(String, String)]): PaypalIPNPostbackReponse =
    new PaypalIPNPostbackReponse(
      PaypalRequest(client(mode, connection), PostMethodFactory("/cgi-bin/webscr", payloadArray(parameters)))
    )
}

/**
 * An abstraction for the response from PayPal during the to and fro of IPN validation
 * @param response The processed List[String] from the paypal IPN request response cycle
 */
private[paypal] class PaypalIPNPostbackReponse(val response: List[String]) extends PaypalResponse {
  def isVerified: Boolean = rawHead match {
    case Some("VERIFIED") => true
    case _ => false
  }
}

