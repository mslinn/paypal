package paypal

import paypal.{PaypalResponse, PaypalRequest, PaypalBase}
import play.api.mvc.RequestHeader

/** Handle the incoming request, dispatch the IPN callback, and handle the subsequent response. */
object PaypalIPN {
  /** @todo Really need to make sure that multiple custom parameters can be mapped through.
    * The current solution is not good! */
  private def paramsAsPayloadList(request: RequestHeader): Seq[(String, String)] =
    (for (p <- request.body.asFormUrlEncoded; mp <- p._2.map(v => (p._1, v))) yield (mp._1, mp._2)).toList

  def apply(request: RequestHeader, mode: PaypalMode, connection: PaypalConnection) = {
    //create request, get response and pass response object to the specified event handlers
    val ipnResponse: PaypalIPNPostbackReponse = PaypalIPNPostback(mode, connection, paramsAsPayloadList(request))
    ipnResponse
  }
}

trait BasePaypalTrait

trait PaypalIPN extends BasePaypalTrait

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

