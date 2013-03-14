/*
 * Copyright 2013 Micronautics Research Corporation
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

import play.api.mvc.RequestHeader
import play.api.Logger

abstract class AbstractPaypalTransactionFinder {
  def findByTxnId(txnId: String): Option[AbstractPaypalTransaction]
}

/** Extend this class for a concrete implementation that can be persisted */
abstract class AbstractPaypalTransaction(dataMap: NameValuePairs) {
  private[controllers] val formNVP = new FormNVP(dataMap)
  val txnId: String = formNVP.maybeGetString("item_name").getOrElse("")
  val receiverEmail: String = formNVP.maybeGetString("receiver_email").getOrElse("")
}

abstract class AbstractTransactionProcessor(txn: String, customerAddress: AbstractCustomerAddress)(implicit request: RequestHeader) {
  def processTransaction: Unit

  def handleDuplicateTransaction(txn: AbstractPaypalTransaction): Unit =
    Logger.info("Ignoring duplicate transaction: " + txn.toString)
}

abstract class AbstractCustomerAddress(dataMap: NameValuePairs)
