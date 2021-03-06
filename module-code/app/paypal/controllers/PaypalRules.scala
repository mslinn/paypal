/*
 * Copyright 2013 Micronautics Research Corporation
 * Portions copyright 2007-2013 WorldWide Conferencing, LLC
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

import java.util.{Currency, Locale}
import play.api.Configuration
import java.io.File

object PaypalRules {
  private lazy val conf = Configuration.load(new File("conf/paypal.conf"))

  lazy val button = conf.getString("button").getOrElse("assets/paypal/en_buynow_68x23.gif")

  lazy val connection: PaypalConnection = PaypalSSL

  lazy val currency: String = Currency.getInstance(Locale.getDefault).getCurrencyCode

  lazy val mode: PaypalMode =
    conf.getString("mode", Some(Set("live", "sandbox"))).getOrElse("sandbox") match {
      case "live" => PaypalLive
      case _ => PaypalSandbox
    }

  lazy val receiverEmail = conf.getString("receiverEmail").getOrElse("nobody@nowhere.com")

  lazy val url =
    if (mode==PaypalLive)
      "https://www.paypal.com/cgi-bin/webscr?cmd=_notify-validate&"
    else
      "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_notify-validate&"
}
