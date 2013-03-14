package paypal.controllers

import paypal._
import play.api._
import play.api.mvc._

object PaypalDataTransfer extends PaypalBase {

  /** @return post body to send, which differs between PDT and IPN */
  private def payloadArray(authToken: String, transactionToken: String) =
    List("cmd" -> "_notify-synch",
         "tx" -> transactionToken,
         "at" -> authToken)

  /**
   * Execute the PDT call
   * @param authToken The token you obtain from the paypal merchant console
   * @param transactionToken The token that is passed back to your application as the "tx" part of the query string
   * @param mode The PaypalMode type that your targeting. Options are PaypalLive or PaypalSandbox
   * @param connection The protocol the invocation is made over. Options are PaypalHTTP or PaypalSSL
   * @return PaypalDataTransferResponse
   */
  def apply(authToken: String, transactionToken: String, mode: PaypalMode, connection: PaypalConnection): PaypalResponse =
    PaypalDataTransferResponse(
      PaypalRequest(client(mode, connection),
                    PostMethodFactory("/cgi-bin/webscr", payloadArray(authToken, transactionToken))))
}

/**
 * Wrapper instance for handling the response from a PayPal data transfer.
 * @param response The processed response List[String]. The response
 * input should be created with StreamResponseProcessor
 */
case class PaypalDataTransferResponse(response: List[String]) extends PaypalResponse {
  def isVerified = paymentSuccessful

  /** Utility method for letting you know if the payment data is returning a successful message */
  def paymentSuccessful: Boolean = rawHead match {
    case Some("SUCCESS") => true
    case _ => false
  }
}

trait PaypalPDT extends Controller with BasePaypalTrait {

  def pdtResponse = {
    case (info, resp) =>
      Logger.info("Got a verified PayPal PDT: "+resp)
      Redirect("/")
  }

  def processPDT = Action { implicit request =>
    // TODO write this - commented out for whomever wants to port this
    /*val formNVP = new FormNVP(request.body.asFormUrlEncoded)
    val redir = for {
      tx <- formNVP.maybeGetOne("tx")
      resp = PaypalDataTransfer(paypalAuthToken, tx, mode, connection)
      info <- resp.paypalInfo
      redir <- tryo(pdtResponse(info, request))
    } yield { redir }
    redir */
    Redirect("/")
  }
}

