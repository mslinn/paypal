/*
 * Copyright 2007-2013 WorldWide Conferencing, LLC
 * Portions copyright 2013 Micronautics Research Corporation
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

import akka.actor.{Props, ActorSystem, Actor}
import collection.mutable.ListBuffer
import concurrent.duration.Duration
import org.apache.commons.httpclient.{HttpClient, NameValuePair}
import org.apache.commons.httpclient.methods._
import java.io._
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import play.api.Logger
import play.api.mvc.{Action, RequestHeader, Controller}

/** A parameter set that takes request parameters and assigns them to properties of this class
 * @param form The parameters from the incoming request */
private[paypal] class PayPalInfo(form: Map[String, Seq[String]]) {
  val formNVP = new FormNVP(form)
  val itemName           = formNVP.maybeGetString("item_name")
  val business           = formNVP.maybeGetString("business")
  val itemNumber         = formNVP.maybeGetString("item_number")
  val paymentStatus: Option[PaypalTransactionStatus.Value] = formNVP.maybeGetString("payment_status").flatMap(PaypalTransactionStatus.find)
  val mcGross            = formNVP.maybeGetString("mc_gross")
  val paymentCurrency    = formNVP.maybeGetString("mc_currency")
  val txnId              = formNVP.maybeGetString("txn_id")
  val receiverEmail      = formNVP.maybeGetString("receiver_email")
  val receiverId         = formNVP.maybeGetString("receiver_id")
  val quantity           = formNVP.maybeGetString("quantity")
  val numCartItems       = formNVP.maybeGetString("num_cart_items")
  val paymentDate        = formNVP.maybeGetString("payment_date")
  val firstName          = formNVP.maybeGetString("first_name")
  val lastName           = formNVP.maybeGetString("last_name")
  val paymentType        = formNVP.maybeGetString("payment_type")
  val paymentGross       = formNVP.maybeGetString("payment_gross")
  val paymentFee         = formNVP.maybeGetString("payment_fee")
  val settleAmount       = formNVP.maybeGetString("settle_amount")
  val memo               = formNVP.maybeGetString("memo")
  val payerEmail         = formNVP.maybeGetString("payer_email")
  val txnType            = formNVP.maybeGetString("txn_type")
  val payerStatus        = formNVP.maybeGetString("payer_status")
  val addressStreet      = formNVP.maybeGetString("address_street")
  val addressCity        = formNVP.maybeGetString("address_city")
  val addressState       = formNVP.maybeGetString("address_state")
  val addressZip         = formNVP.maybeGetString("address_zip")
  val addressCountry     = formNVP.maybeGetString("address_country")
  val addressStatus      = formNVP.maybeGetString("address_status")
  val tax                = formNVP.maybeGetString("tax")
  val optionName1        = formNVP.maybeGetString("option_name1")
  val optionSelection1   = formNVP.maybeGetString("option_selection1")
  val optionName2        = formNVP.maybeGetString("option_name2")
  val optionSelection2   = formNVP.maybeGetString("option_selection2")
  val forAuction         = formNVP.maybeGetString("for_auction")
  val invoice            = formNVP.maybeGetString("invoice")
  val custom             = formNVP.maybeGetString("custom")
  val notifyVersion      = formNVP.maybeGetString("notify_version")
  val verifySign         = formNVP.maybeGetString("verify_sign")
  val payerBusinessName  = formNVP.maybeGetString("payer_business_name")
  val payerId            = formNVP.maybeGetString("payer_id")
  val mcCurrency         = formNVP.maybeGetString("mc_currency")
  val mcFee              = formNVP.maybeGetString("mc_fee")
  val exchangeRate       = formNVP.maybeGetString("exchange_rate")
  val settleCurrency     = formNVP.maybeGetString("settle_currency")
  val parentTxnId        = formNVP.maybeGetString("parent_txn_id")
  val pendingReason      = formNVP.maybeGetString("pending_reason")
  val reasonCode         = formNVP.maybeGetString("reason_code")
  val subscrId           = formNVP.maybeGetString("subscr_id")
  val subscrDate         = formNVP.maybeGetString("subscr_date")
  val subscrEffective    = formNVP.maybeGetString("subscr_effective")
  val period1            = formNVP.maybeGetString("period1")
  val period2            = formNVP.maybeGetString("period2")
  val period3            = formNVP.maybeGetString("period3")
  val amount             = formNVP.maybeGetString("amt")
  val amount1            = formNVP.maybeGetString("amount1")
  val amount2            = formNVP.maybeGetString("amount2")
  val amount3            = formNVP.maybeGetString("amount3")
  val mcAmount1          = formNVP.maybeGetString("mc_amount1")
  val mcAmount2          = formNVP.maybeGetString("mc_amount2")
  val mcAmount3          = formNVP.maybeGetString("mcamount3")
  val recurring          = formNVP.maybeGetString("recurring")
  val reattempt          = formNVP.maybeGetString("reattempt")
  val retryAt            = formNVP.maybeGetString("retry_at")
  val recurTimes         = formNVP.maybeGetString("recur_times")
  val username           = formNVP.maybeGetString("username")
  val password           = formNVP.maybeGetString("password")

  val auctionClosingDate = formNVP.maybeGetString("auction_closing_date")
  val auctionMultiItem   = formNVP.maybeGetString("auction_multi_item")
  val auctionBuyerId     = formNVP.maybeGetString("auction_buyer_id")

  override def toString: String = {
    val s1 = "itemName={"+ itemName +"}, business={"+ business +"}, itemNumber={"+ itemNumber +"}, paymentStatus={"+
      paymentStatus +"}, mcGross={"+ mcGross +"}, paymentCurrency={"+ paymentCurrency +"}, txnId={"+ txnId +
      "}, receiverEmail={"+ receiverEmail
    val s2 = "}, receiverId={"+ receiverId +"}, quantity={"+ quantity +"}, numCartItems={"+ numCartItems +
      "}, paymentDate={"+ paymentDate +"}, firstName={"+ firstName +"}, lastName={"+ lastName +"}, paymentType={"+
      paymentType +"}, paymentGross={"+ paymentGross +"}, paymentFee={"+ paymentFee +"}, settleAmount={"+ settleAmount +
      "}, memo={"+ memo +"}, payerEmail={"+ payerEmail
    val s3 = "}, txnType={"+ txnType +"}, payerStatus={"+ payerStatus +"}, addressStreet={"+ addressStreet +
      "}, addressCity={"+ addressCity +"}, addressState={"+ addressState +"}, addressZip={"+ addressZip +
      "}, addressCountry={"+ addressCountry +"}, addressStatus={"+ addressStatus +"}, tax={"+ tax +"}, optionName1={"+
      optionName1 +"}, optionSelection1={"+ optionSelection1 +"}, optionName2={"+ optionName2 +"}, optionSelection2={"+ optionSelection2
    val s4 = "}, forAuction={"+ forAuction +"}, invoice={"+ invoice +"}, custom={"+ custom +"}, notifyVersion={"+
      notifyVersion +"}, verifySign={"+ verifySign +"}, payerBusinessName={"+ payerBusinessName +"}, payerId={"+
      payerId +"}, mcCurrency={"+ mcCurrency +"}, mcFee={"+ mcFee +"}, exchangeRate={"+ exchangeRate +"}, settleCurrency={"+ settleCurrency
    val s5 = "}, parentTxnId={"+ parentTxnId +"}, pendingReason={"+ pendingReason +"}, reasonCode={"+ reasonCode +
      "}, subscrId={"+ subscrId +"}, subscrDate={"+ subscrDate +"}, subscrEffective={"+ subscrEffective +"}, period1={"+
      period1+"}, period2={"+period2+"}, period3={"+period3+"}, amount={"+ amount +"}, amount={"+amount1+"}, amount2={"+
      amount2+"}, amount3={"+amount3
    val s6 = "}, mcAmount1={"+mcAmount1+"}, mcAmount2={"+mcAmount2+"}, mcAmount3={"+mcAmount3+"},recurring={"+ recurring +
      "}, reattempt,retryAt={"+ retryAt +"}, recurTimes,username={"+ username +"},password={"+ password +
      "}, auctionClosingDate={"+ auctionClosingDate +"}, auctionMultiItem={"+ auctionMultiItem +"}, auctionBuyerId={"+auctionBuyerId+"}"
    s1 + s2 + s3 + s4 + s5 + s6
  }
}

object PayPalController extends Controller {
  implicit lazy val system = ActorSystem.create()
  lazy val requestQueue = system.actorOf(Props[RequestQueue])
  requestQueue ! PingMe

  def paypalAuthToken: String

  def processIPN = Action { implicit request =>
    requestQueue ! IPNRequest(request, 0, System.currentTimeMillis)
    Ok
  }

  protected case class IPNRequest(request: RequestHeader, cnt: Int, when: Long)

  protected case object PingMe

  protected def buildInfo(resp: PaypalResponse, request: RequestHeader): Option[PayPalInfo] = {
    if (resp.isVerified) Some(new PayPalInfo(request)) else None
  }

  /** Ported from LiftActor to Akka 2.1 actor */
  protected class RequestQueue extends Actor {
    lazy val tenSeconds = Duration.create(10, TimeUnit.SECONDS)

    /** Number of times to attempt to verify the request */
    lazy val MaxRetry = 6

    def receive = {
      case PingMe =>
        context.system.scheduler.scheduleOnce(tenSeconds, self, PingMe)

      case IPNRequest(_, count, _) if count > MaxRetry => // discard the transaction

      case IPNRequest(response, count, when) if when <= System.currentTimeMillis =>
        try {
          val resp = PaypalIPN(response, PaypalRules.mode, PaypalRules.connection)
          buildInfo(resp, response)).map { info: PayPalInfo =>
            actions((info.paymentStatus, info, response))
          }
        } catch {
          case _ => // retry
              self ! IPNRequest(response, count + 1, System.currentTimeMillis + (1000 * 8 << (count + 2)))
        }
    }
  }
}
