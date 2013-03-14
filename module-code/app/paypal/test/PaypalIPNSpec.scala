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

class PaypalTransaction(dataMap: NameValuePairs) extends AbstractPaypalTransaction(dataMap)

class PaypalTransactionFinder extends AbstractPaypalTransactionFinder {
  def findByTxnId(txnId: String): Option[PaypalTransaction] = {
    ApplicationServicesSpec.dataMaps.get(txnId).map(new PaypalTransaction(_))
  }
}

class CustomerAddress(dataMap: NameValuePairs) extends AbstractCustomerAddress(dataMap)

class TransactionProcessor(txn: PaypalTransaction, customerAddress: CustomerAddress)
  extends AbstractTransactionProcessor(txn, customerAddress) {
  def processTransaction: Unit = {
    println("Bogus processTransaction")
  }
}

object ApplicationServicesSpec {
  def makeDataMap(txnId: String, firstName: String, lastName: String, payerEmail: String) = Map(
    ("item_name", List("Dog bones")),
    ("item_number", List("1")),
    ("txn_id", List(txnId)),
    ("receiver_email", List(PaypalRules.receiverEmail)),
    ("quantity", List("1")),
    ("num_cart_items", List("1")),
    ("payment_date", List("2013-03-14")),
    ("first_name", List(firstName)),
    ("last_name", List(lastName)),
    ("payment_type", List("instant")),
    ("payment_gross", List("123.45")),
    ("payment_fee", List("1.23")),
    ("payer_email", List(payerEmail)),
    ("address_street", List("301 Cobblestone Wy.")),
    ("address_city", List("Bedrock")),
    ("address_state", List("CA")),
    ("address_zip", List("70777")),
    ("address_country", List("USA")),
    ("custom", List("whatever")),
    ("payer_id", List("12345")),
    ("mc_currency", List("usd"))
  )

  val dm1 = makeDataMap("1234", "Fred",   "Flintstone", "fred@flintstone.com")
  val dm2 = makeDataMap("1235", "Wilma",  "Flintstone", "wilma@flintstone.com")
  val dm3 = makeDataMap("1236", "Barney", "Rubble",     "barney@rubble.com")
  val dm4 = makeDataMap("1237", "Betty",  "Rubble",     "betty@rubble.com")
  val dataMaps = List(dm1, dm2, dm3, dm4).map(x => (x.get("txn_id").get.head, x)).toMap[String, Map[String, List[String]]]

  implicit def pptFinder(): PaypalTransactionFinder = new PaypalTransactionFinder

  implicit def pptFactory(dataMap: NameValuePairs): PaypalTransaction = new PaypalTransaction(dataMap)

  implicit def caFactory(dataMap: NameValuePairs): CustomerAddress = new CustomerAddress(dataMap)

  implicit def tpFactory(txn: PaypalTransaction, customerAddress: CustomerAddress): TransactionProcessor =
    new TransactionProcessor(txn, customerAddress)

  //((null:NameValuePairs):PaypalTransaction)
}

class ApplicationServicesSpec extends Specification {
  import ApplicationServicesSpec._

  implicit val dataMap: NameValuePairs = dm1

  val ipn = new PaypalIPN[PaypalTransactionFinder, PaypalTransaction, CustomerAddress, TransactionProcessor]

  "IPN responses" should {
    "stand on their heads and chew gum" in {
      dataMap must equalTo(dm1)
    }
  }
}

// object SimplePaypal extends PaypalIPN {
//   def actions = {
//     case (status, info, resp) =>
//       Log.info("Got a verified PayPal IPN: "+status)
//   }
// }
