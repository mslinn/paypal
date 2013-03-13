PayPal Play Module
==================

This is a port in progress of the Liftweb PayPal module.
Nothing works yet.
Adapted from the [LiftWeb module](https://github.com/liftmodules/paypal) of the same name, and rewritten using features
of Scala 2.10, Play 2.1 and Akka 2.10.
The Liftweb module supports both PDT (Payment Data Transfer):
![PDT - Payment Data Transfer](https://www.paypal.com/en_US/i/IntegrationCenter/scr/scr_ppPDTDiagram_513x282.gif)

and IPN (Instant Payment Notification):
![IPN -Instant Payment Notification](https://www.paypal.com/en_US/i/IntegrationCenter/scr/scr_ppIPNDiagram_555x310.gif).
Section 13.3 of ["Exploring LiftWeb"](http://exploring.liftweb.net/master/index-13.html#toc-Section-13.3) discusses the PayPal module.

This Play module will initially only provide integration with PayPal IPN; someone else can complete the port of PDT.
I left the code in, but I won't test it because I do not need PDT.

PDT Example
-----------

These examples are completely wrong because the code is still 80% Lift and only 20% Play framework 2.1 right now.
I'll revisit these docs when I've got something working.

````
import controllers.paypal._
​
object MyPayPalPDT extends PayPalPDT {
  override def pdtPath = "paypal_complete"
  def paypalAuthToken = Props.get("paypal.authToken") openOr "cannot find auth token from props file"
​
  def pdtResponse: PartialFunction[(PayPalInfo, Req), LiftResponse] = {
    case (info, req) => println("— in pdtResponse"); DoRedirectResponse("/account_admin/index");
  }
}
​````

`pdtResponse` allows you to determine the behavior of you application upon receiving the reponse from PayPal.

IPN Example
-----------

````
import controllers.paypal._
​
object MyPayPalIPN extends PayPalIPN {
  def actions = {
    case (ClearedPayment, info, req) => // do your processing here
    case (RefundedPayment, info, req) => // do refund processing
  }
}
````​

Pattern match on the PaypalTransactionStatus.
IPN is a ’machine-to-machine’ API which happens in the background without end user interaction.
