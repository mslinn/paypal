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

package paypal.controllers

import paypal._
import play.api._
import play.api.mvc._

object PaypalDataTransfer /*extends PaypalBase */{

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
  def apply(authToken: String, transactionToken: String, mode: PaypalMode, connection: PaypalConnection): Unit = {} //PaypalResponse =
//    PaypalDataTransferResponse(
//      PaypalRequest(client(mode, connection),
//                    PostMethodFactory("/cgi-bin/webscr", payloadArray(authToken, transactionToken))))
}

trait PaypalPDT extends Controller with BasePaypalTrait {

  def pdtResponse = Action { implicit request =>
    Logger.info(s"Got a verified PayPal PDT: ${request.body.asText}")
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

