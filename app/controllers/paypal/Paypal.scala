/*
 * Copyright 2007-2013 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package paypal

import java.io._
import collection.mutable.ListBuffer
import org.apache.commons.httpclient.{HttpClient, NameValuePair}
import org.apache.commons.httpclient.methods._
import java.net.URLDecoder
import play.api.Logger
import play.api.mvc.{RequestHeader, Controller}
import akka.actor.Actor


/**
 * Sealed abstract type PaypalMode so we can cast to the super class in our method declarations.
 * Cannot be subclassed outside of this source file.
 */
sealed trait PaypalMode {
  def domain: String

  override def toString = s"PaypalMode: $domain"
}

object PaypalSandbox extends PaypalMode {
  val domain = "www.sandbox.paypal.com"
}

object PaypalLive extends PaypalMode {
  val domain = "www.paypal.com"
}

/** Represents the type of connection that can be made to PayPal, irrespective of the mode of connection */
sealed trait PaypalConnection {
  def protocol: String
  val port: Int = 80

  override def toString = s"PaypalConnection: ${protocol}:${port}"
}

object PaypalHTTP extends PaypalConnection {
  val protocol = "http"
}

object PaypalSSL extends PaypalConnection {
  val protocol = "https"
  override val port: Int = 443
}

/**
 * Contains all the paypal status abstractions as enumerable vals
 */
object PaypalTransactionStatus extends Enumeration {
  val CancelledReversalPayment = Value(1, "Cancelled_Reversal")
  val ClearedPayment = Value(2, "Cleared")
  val CompletedPayment = Value(3, "Completed")
  val DeniedPayment = Value(4, "Denied")
  val ExpiredPayment = Value(5, "Expired")
  val FailedPayment = Value(6, "Failed")
  val PendingPayment = Value(7, "Pending")
  val RefundedPayment = Value(8, "Refunded")
  val ReturnedPayment = Value(9, "Returned")
  val ReversedPayment = Value(10, "Reversed")
  val UnclaimedPayment = Value(11, "Unclaimed")
  val UnclearedPayment = Value(12, "Uncleared")
  val VoidedPayment = Value(13, "Voided")
  val InProgressPayment = Value(14, "In-Progress")
  val PartiallyRefundedPayment = Value(15, "Partially-Refunded")
  val ProcessedPayment = Value(16, "Processed")

  def find(name: String): Option[Value] = {
    val n = name.trim.toLowerCase
    values.filter(_.toString.toLowerCase == n).headOption
  }
}

/**
 * A parameter set that takes request parameters and assigns them
 * to properties of this class
 *
 * @param form The parameters from the incoming request
 */
class PayPalInfo(form: Map[String, Seq[String]]) {

  private def get(name: String): Seq[String] = {
    val strArray: Seq[String] = (for {
      strings <- form.get(name).toSeq
      string <- strings.toSeq
    } yield string).toSeq
    val result: Seq[String] = if (strArray.size==1 && strArray.head=="") Nil else strArray
    result
  }

  /** @return first element of form field as an Option[String] */
  private def maybeGetOne(name: String): Option[String] = get(name).headOption

  val itemName = maybeGetOne("item_name")
  val business = maybeGetOne("business")
  val itemNumber = maybeGetOne("item_number")
  val paymentStatus: Option[PaypalTransactionStatus.Value] = maybeGetOne("payment_status").flatMap(PaypalTransactionStatus.find)
  val mcGross = maybeGetOne("mc_gross")
  val paymentCurrency = maybeGetOne("mc_currency")
  val txnId = maybeGetOne("txn_id")
  val receiverEmail = maybeGetOne("receiver_email")
  val receiverId = maybeGetOne("receiver_id")
  val quantity = maybeGetOne("quantity")
  val numCartItems = maybeGetOne("num_cart_items")
  val paymentDate = maybeGetOne("payment_date")
  val firstName = maybeGetOne("first_name")
  val lastName = maybeGetOne("last_name")
  val paymentType = maybeGetOne("payment_type")
  val paymentGross = maybeGetOne("payment_gross")
  val paymentFee = maybeGetOne("payment_fee")
  val settleAmount = maybeGetOne("settle_amount")
  val memo = maybeGetOne("memo")
  val payerEmail = maybeGetOne("payer_email")
  val txnType = maybeGetOne("txn_type")
  val payerStatus = maybeGetOne("payer_status")
  val addressStreet = maybeGetOne("address_street")
  val addressCity = maybeGetOne("address_city")
  val addressState = maybeGetOne("address_state")
  val addressZip = maybeGetOne("address_zip")
  val addressCountry = maybeGetOne("address_country")
  val addressStatus = maybeGetOne("address_status")
  val tax = maybeGetOne("tax")
  val optionName1 = maybeGetOne("option_name1")
  val optionSelection1 = maybeGetOne("option_selection1")
  val optionName2 = maybeGetOne("option_name2")
  val optionSelection2 = maybeGetOne("option_selection2")
  val forAuction = maybeGetOne("for_auction")
  val invoice = maybeGetOne("invoice")
  val custom = maybeGetOne("custom")
  val notifyVersion = maybeGetOne("notify_version")
  val verifySign = maybeGetOne("verify_sign")
  val payerBusinessName = maybeGetOne("payer_business_name")
  val payerId =maybeGetOne("payer_id")
  val mcCurrency = maybeGetOne("mc_currency")
  val mcFee = maybeGetOne("mc_fee")
  val exchangeRate = maybeGetOne("exchange_rate")
  val settleCurrency  = maybeGetOne("settle_currency")
  val parentTxnId  = maybeGetOne("parent_txn_id")
  val pendingReason = maybeGetOne("pending_reason")
  val reasonCode = maybeGetOne("reason_code")
  val subscrId = maybeGetOne("subscr_id")
  val subscrDate = maybeGetOne("subscr_date")
  val subscrEffective  = maybeGetOne("subscr_effective")
  val period1 = maybeGetOne("period1")
  val period2 = maybeGetOne("period2")
  val period3 = maybeGetOne("period3")
  val amount = maybeGetOne("amt")
  val amount1 = maybeGetOne("amount1")
  val amount2 = maybeGetOne("amount2")
  val amount3 = maybeGetOne("amount3")
  val mcAmount1 = maybeGetOne("mc_amount1")
  val mcAmount2 = maybeGetOne("mc_amount2")
  val mcAmount3 = maybeGetOne("mcamount3")
  val recurring = maybeGetOne("recurring")
  val reattempt = maybeGetOne("reattempt")
  val retryAt = maybeGetOne("retry_at")
  val recurTimes = maybeGetOne("recur_times")
  val username = maybeGetOne("username")
  val password = maybeGetOne("password")

  val auctionClosingDate  = maybeGetOne("auction_closing_date")
  val auctionMultiItem  = maybeGetOne("auction_multi_item")
  val auctionBuyerId  = maybeGetOne("auction_buyer_id")
  override def toString: String = {
  val s1 = "itemName={"+ itemName +"}, business={"+ business +"}, itemNumber={"+ itemNumber +"}, paymentStatus={"+ paymentStatus +"}, mcGross={"+ mcGross +"}, paymentCurrency={"+ paymentCurrency +"}, txnId={"+ txnId +"}, receiverEmail={"+ receiverEmail
  val s2 = "}, receiverId={"+ receiverId +"}, quantity={"+ quantity +"}, numCartItems={"+ numCartItems +"}, paymentDate={"+ paymentDate +"}, firstName={"+ firstName +"}, lastName={"+ lastName +"}, paymentType={"+ paymentType +"}, paymentGross={"+ paymentGross +"}, paymentFee={"+ paymentFee +"}, settleAmount={"+ settleAmount +"}, memo={"+ memo +"}, payerEmail={"+ payerEmail
  val s3 = "}, txnType={"+ txnType +"}, payerStatus={"+ payerStatus +"}, addressStreet={"+ addressStreet +"}, addressCity={"+ addressCity +"}, addressState={"+ addressState +"}, addressZip={"+ addressZip +"}, addressCountry={"+ addressCountry +"}, addressStatus={"+ addressStatus +"}, tax={"+ tax +"}, optionName1={"+ optionName1 +"}, optionSelection1={"+ optionSelection1 +"}, optionName2={"+ optionName2 +"}, optionSelection2={"+ optionSelection2
  val s4 = "}, forAuction={"+ forAuction +"}, invoice={"+ invoice +"}, custom={"+ custom +"}, notifyVersion={"+ notifyVersion +"}, verifySign={"+ verifySign +"}, payerBusinessName={"+ payerBusinessName +"}, payerId={"+ payerId +"}, mcCurrency={"+ mcCurrency +"}, mcFee={"+ mcFee +"}, exchangeRate={"+ exchangeRate +"}, settleCurrency={"+ settleCurrency
  val s5 = "}, parentTxnId={"+ parentTxnId +"}, pendingReason={"+ pendingReason +"}, reasonCode={"+ reasonCode +"}, subscrId={"+ subscrId +"}, subscrDate={"+ subscrDate +"}, subscrEffective={"+ subscrEffective +"}, period1={"+period1+"}, period2={"+period2+"}, period3={"+period3+"}, amount={"+ amount +"}, amount={"+amount1+"}, amount2={"+amount2+"}, amount3={"+amount3
  val s6 = "}, mcAmount1={"+mcAmount1+"}, mcAmount2={"+mcAmount2+"}, mcAmount3={"+mcAmount3+"},recurring={"+ recurring +"}, reattempt,retryAt={"+ retryAt +"}, recurTimes,username={"+ username +"},password={"+ password +"}, auctionClosingDate={"+ auctionClosingDate +"}, auctionMultiItem={"+ auctionMultiItem +"}, auctionBuyerId={"+auctionBuyerId+"}"
  s1 + s2 + s3 + s4 + s5 + s6
  }
}

/** As the HTTP Commons HttpClient class is by definition very mutable, we provide this factory method to produce an
  * instance that we can assign to a val */
private object HttpClientFactory {
  /** @param url The url you are sending to
    * @param port The TCP port the message will be sent over
    * @param connection The protocal to use: http, or https */
  def apply(url: String, port: Int, connection: String): HttpClient = {
    val c: HttpClient = new HttpClient()
    c.getParams().setParameter("http.protocol.content-charset", "UTF-8")
    c.getHostConfiguration().setHost(url, port, connection)
    c
  }
}

private object PostMethodFactory {
  /**
   * Creates a new PostMethod and applies the passed parameters
   * @param url The string representation of the endpoint (e.g. www.paypal.com)
   * paypal parameters A Seq[(String,String)] of parameters that will become the payload of the request
   */
  def apply(url: String, parameters: Seq[(String, String)]): PostMethod = {
    val p: PostMethod = new PostMethod(url)
    p.setRequestBody(parameters)
    p
  }

  implicit def tonvp(in: Seq[(String, String)]): Array[NameValuePair] =
    in.map(p => new NameValuePair(p._1, p._2)).toArray
}

/** Common functionality for paypal PDT and IPN */
trait PaypalBase {
  /**
   * Create a new HTTP client
   * @param mode The PaypalMode type that your targeting. Options are PaypalLive or PaypalSandbox
   * @param connection The protocol the invocation is made over. Options are PaypalHTTP or PaypalSSL
   */
  protected def client(mode: PaypalMode, connection: PaypalConnection): HttpClient =
    HttpClientFactory(mode.domain, connection.port, connection.protocol)
}

/**
 * A simple abstraction for all HTTP operations. By definition they will return a HTTP error
 * code. We are invariably only concerned with if it was a good one or not.
 */
trait PaypalUtilities {
  def wasSuccessful(code: Int): Boolean = code match {
    case 200 => true
    case _ => false
  }
}

/** All HTTP requests to the paypal servers must subclass PaypalRequest. */
private object PaypalRequest extends PaypalUtilities {
  /**
    * @param post Specify the payload of the HTTP request. Must be an instance of PostMethod from HTTP commons
    * @param client Must be a HTTP client; the simplest way to create this is by using HttpClientFactory */
  def apply(client: HttpClient, post: PostMethod): List[String] = wasSuccessful(tryo(client.executeMethod(post)).openOr(500)) match {
    case true => StreamResponseProcessor(post)
    case _ => List("Failure")
  }
}

/**
 * As InputStream is a mutable I/O, we need to use a singleton to access it, process it and return a immutable result.
 * If we did not do this then we get null pointers. */
private object StreamResponseProcessor {
  /** @param p PostMethod Takes the raw HTTP commons PostMethod and processes its stream response */
  def apply(p: PostMethod): List[String] = {
    val stream: InputStream = p.getResponseBodyAsStream()
    val reader: BufferedReader = new BufferedReader(new InputStreamReader(stream))
    val ret: ListBuffer[String] = new ListBuffer

    try {
      def doRead {
        reader.readLine() match {
          case null => ()
          case line =>
            ret += line
            doRead
        }
      }

      doRead
      ret.toList
    } catch {
      case _ : Throwable => Nil
    }
  }
}


/** All paypal service classes need to subclass PaypalResponse. */
trait PaypalResponse extends PaypalUtilities {
  def response: List[String]
  def isVerified: Boolean

  private lazy val info: Map[String, String] =
    Map((for (v <- response; s <- split(v)) yield s) :_*)

  def param(name: String): Option[String] = info.get(name)

  lazy val paypalInfo: Option[PayPalInfo] =
    if (isVerified) Some(new PayPalInfo(this)) else None

  def rawHead: Option[String] = response.headOption

  private def split(in: String): Option[(String, String)] = {
    val pos = in.indexOf("=")
    if (pos < 0) None
      else Some((URLDecoder.decode(in.substring(0, pos), "UTF-8"),
                 URLDecoder.decode(in.substring(pos + 1), "UTF-8")))
  }
}


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

//
// PAYPAL IPN
//

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

object SimplePaypal extends PaypalIPN with PaypalPDT {
  lazy val paypalAuthToken = "123"

  def actions = {
    case (status, info, resp) =>
      Logger.info("Got a verified PayPal IPN: "+status)
  }

  def pdtResponse = {
    case (info, resp) =>
      Logger.info("Got a verified PayPal PDT: "+resp)
      DoRedirectResponse.apply("/")
  }
}

object PayPalController extends Controller {
  def paypalAuthToken: String

  def processPDT(r: Req)(): Option[LiftResponse] = {
    for (tx <- maybeGetOne("tx");
         resp = PaypalDataTransfer(paypalAuthToken, tx, mode, connection);
         info <- resp.paypalInfo;
         redir <- tryo(pdtResponse(info, r))) yield {
      redir
    }
  }

  def defaultResponse(): Option[LiftResponse] = Full(PlainTextResponse("ok"))

  override def dispatch: List[LiftRules.DispatchPF] = {
    val nf: LiftRules.DispatchPF = NamedPF("Default PaypalIPN") {
      case r @ Req(RootPath :: IPNPath :: Nil, "", PostRequest) =>
      r.params // force the lazy value to be evaluated
      requestQueue ! IPNRequest(r, 0, millis)
      defaultResponse _
    }

    super.dispatch ::: List(nf)
  }

  def actions: PartialFunction[(Option[PaypalTransactionStatus.Value], PayPalInfo, Req), Unit]

  protected case class IPNRequest(r: Req, cnt: Int, when: Long)
  protected case object PingMe


  protected def buildInfo(resp: PaypalResponse, req: Req): Option[PayPalInfo] = {
    if (resp.isVerified) Some(new PayPalInfo(req))
    else None
  }

  /** Number of times to attempt to verify the request */
  lazy val MaxRetry = 6

  protected object requestQueue extends Actor {
    protected def messageHandler = {
      case PingMe => Schedule.schedule(this, PingMe, 10 seconds)

      case IPNRequest(r, cnt, _) if cnt > MaxRetry => // discard the transaction

      case IPNRequest(r, cnt, when) if when <= millis =>
        try {
          val resp = PaypalIPN(r, mode, connection)

          for (info <-  buildInfo(resp, r)) yield {
            actions((info.paymentStatus, info, r))
            true
          }
        } catch {
          case _ => // retry
              this ! IPNRequest(r, cnt + 1, millis + (1000 * 8 << (cnt + 2)))
        }
      }
  }
  requestQueue ! PingMe
}
