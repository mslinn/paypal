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

package object controllers {
  type NameValuePairs = Map[String, Seq[String]]


  /** Sealed abstract type PaypalMode so we can cast to the super class in our method declarations.
   * Cannot be subclassed outside of this source file. */
  sealed trait PaypalMode {
    val domain: String

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
    val protocol: String
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
    val CancelledReversalPayment = Value(1,  "Cancelled_Reversal")
    val ClearedPayment           = Value(2,  "Cleared")
    val CompletedPayment         = Value(3,  "Completed")
    val DeniedPayment            = Value(4,  "Denied")
    val ExpiredPayment           = Value(5,  "Expired")
    val FailedPayment            = Value(6,  "Failed")
    val PendingPayment           = Value(7,  "Pending")
    val RefundedPayment          = Value(8,  "Refunded")
    val ReturnedPayment          = Value(9,  "Returned")
    val ReversedPayment          = Value(10, "Reversed")
    val UnclaimedPayment         = Value(11, "Unclaimed")
    val UnclearedPayment         = Value(12, "Uncleared")
    val VoidedPayment            = Value(13, "Voided")
    val InProgressPayment        = Value(14, "In-Progress")
    val PartiallyRefundedPayment = Value(15, "Partially-Refunded")
    val ProcessedPayment         = Value(16, "Processed")

    def find(name: String): Option[Value] = {
      val n = name.trim.toLowerCase
      values.filter(_.toString.toLowerCase == n).headOption
    }
  }
}
