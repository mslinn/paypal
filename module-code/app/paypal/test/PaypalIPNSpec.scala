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

import org.specs2.mutable._
import play.api.mvc.RequestHeader

class PaypalTransaction(dataMap: NameValuePairs) extends AbstractPaypalTransaction(dataMap)

class PaypalTransactionFinder extends AbstractPaypalTransactionFinder {
  def findByTxnId(txnId: String): Option[PaypalTransaction] = Some(new PaypalTransaction(Map.empty)) // todo make this useful
}

class CustomerAddress(dataMap: NameValuePairs) extends AbstractCustomerAddress(dataMap)

class TransactionProcessor(txn: PaypalTransaction, customerAddress: CustomerAddress)
  extends AbstractTransactionProcessor(txn, customerAddress) {
  def processTransaction: Unit = { println("Bogus processTransaction") }
}

object ApplicationServicesSpec {
  def pptFinder(): PaypalTransactionFinder = new PaypalTransactionFinder
  implicit def pptFactory(dataMap: NameValuePairs): PaypalTransaction = new PaypalTransaction(dataMap)
  implicit def caFactory(dataMap: NameValuePairs): CustomerAddress = new CustomerAddress(dataMap)
  implicit def tpFactory(txn: PaypalTransaction, customerAddress: CustomerAddress): TransactionProcessor =
    new TransactionProcessor(txn, customerAddress)

  ((null:NameValuePairs):PaypalTransaction)
}


class ApplicationServicesSpec extends Specification {
  import ApplicationServicesSpec._

  implicit val dataMap: NameValuePairs = Map.empty

  val ipn = new PaypalIPN[PaypalTransactionFinder, PaypalTransaction, CustomerAddress, TransactionProcessor](pptFinder)(pptFactory, caFactory, tpFactory)

   "IPN responses" should {
     "have a boxed transaction status" in {
       true // todo write this
     }
   }
}

// object SimplePaypal extends PaypalIPN {
//   def actions = {
//     case (status, info, resp) =>
//       Log.info("Got a verified PayPal IPN: "+status)
//   }
// }
