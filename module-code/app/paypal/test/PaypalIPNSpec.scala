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

import org.specs.Specification
import play.api.mvc.RequestHeader

// import org.specs.mock._
// import org.specs.mock.JMocker._
import org.specs.runner.JUnit4

class PaypalTransaction(dataMap: Map[String, Seq[String]]) extends AbstractPaypalTransaction(dataMap)

class CustomerAddress(dataMap: Map[String, Seq[String]]) extends AbstractCustomerAddress(dataMap)

class TransactionProcessor(txn: String, customerAddress: CustomerAddress)(implicit request: RequestHeader)
  extends AbstractTransactionProcessor(txn, customerAddress)(request)
{
  def processTransaction: Unit = { println("Bogus processTransaction") }
}

object Factories {
  implicit def pptFactory[PaypalTransaction](implicit dataMap: Map[String, Seq[String]]) = new PaypalTransaction(dataMap)
  implicit def caFactory[CustomerAddress](implicit dataMap: Map[String, Seq[String]]) = new CustomerAddress(dataMap)
  implicit def tpFactory[TransactionProcessor](txn: String, customerAddress: CustomerAddress)(implicit request: RequestHeader) =
    new TransactionProcessor(txn, customerAddress)(request)
}

//class PaypalIPNSpecTest extends JUnit4(PaypalIPNSpec)

object ApplicationServicesSpec
   extends Specification("Paypal IPN")
//   with JMocker
//   with ClassMocker
  {
  implicit val dataMap: Map[String, Seq[String]] = Map.empty
  import Factories._
  val ipn = new PaypalIPN[PaypalTransaction, CustomerAddress, TransactionProcessor]
//
//   "IPN responses" should {
//     "have a boxed transaction status" in {
//
//     }
//   }
//
}
//
// object SimplePaypal extends PaypalIPN {
//   def actions = {
//     case (status, info, resp) =>
//       Log.info("Got a verified PayPal IPN: "+status)
//   }
// }
