package org.migor.feedless.plan

import com.stripe.model.Card
import com.stripe.model.Customer
import com.stripe.model.Dispute
import com.stripe.model.EventDataObjectDeserializer
import com.stripe.model.Invoice
import com.stripe.model.Subscription
import com.stripe.net.Webhook
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.*


@Controller
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class PaymentController(
  private val orderService: OrderService,
  private val paymentService: PaymentService,
  private val propertyService: PropertyService,
  @Value("\${app.stripeSecretKey}")
  private val stripeApiKey: String,
  @Value("\${app.stripeWebhookSecret}")
  private val stripeWebhookSecret: String
) {

  private val log = LoggerFactory.getLogger(PaymentController::class.simpleName)

//  @GetMapping(
//    "/payment/{billingId}/callback",
//  )
//  suspend fun paymentCallback(
//    @PathVariable("billingId") billingId: String,
//  ): ResponseEntity<String> = coroutineScope {
//    val corrId = kotlin.coroutines.coroutineContext.corrId()
//    log.info("[$corrId] paymentCallback $billingId")
//    val headers = HttpHeaders()
//    val queryParams = try {
//      orderService.handlePaymentCallback(billingId)
//      "success=true"
//    } catch (ex: Exception) {
//      log.error("Payment callback failed with ${ex.message}", ex)
//      "success=false&message=${ex.message}"
//    }
//    headers.add(HttpHeaders.LOCATION, "${propertyService.appHost}/payment/summary/${billingId}?$queryParams")
//    ResponseEntity<String>(headers, HttpStatus.FOUND)
//  }

  @GetMapping(
    "/checkout/{orderId}",
    "/checkout/{orderId}/",
  )
  suspend fun createCheckout(@PathVariable("orderId") orderId: String): ResponseEntity<String> = coroutineScope {
    val session = paymentService.createCheckoutSession(UUID.fromString(orderId))
//    log.info("[$corrId] paymentCallback $billingId")
    val headers = HttpHeaders()
    headers.add(HttpHeaders.LOCATION, session.url)
    ResponseEntity<String>(headers, HttpStatus.FOUND)
  }

  @PostMapping(
    "/payment/webhook",
  )
  suspend fun webhook(@RequestBody payload: String, request: HttpServletRequest): ResponseEntity<String> =
    coroutineScope {
      try {
        val sigHeader = request.getHeader("stripe-signature")
        val event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret)
        val dataObjectDeserializer: EventDataObjectDeserializer = event.dataObjectDeserializer
        if (dataObjectDeserializer.getObject().isPresent) {
          val stripeObject = dataObjectDeserializer.getObject().get()
          when (event.type) {
            "charge.dispute.created" -> paymentService.onDisputeCreated(stripeObject as Dispute)
            "account.external_account.deleted" -> paymentService.onUserDeleted(stripeObject as Card)
            "customer.deleted" -> paymentService.onCustomerDeleted(stripeObject as Customer)
            "customer.subscription.deleted" -> paymentService.onSubscriptionDeleted(stripeObject as Subscription)
            "customer.subscription.paused" -> paymentService.onSubscriptionPaused(stripeObject as Subscription)
            "issuing_dispute.submitted" -> paymentService.onDisputeSubmitted(stripeObject as Dispute)
            "customer.subscription.created" -> paymentService.onSubscriptionCreated(stripeObject as Subscription)
            "invoice.created" -> paymentService.onInvoiceCreated(stripeObject as Invoice)
            "invoice.payment_succeeded" -> paymentService.onPaymentSucceeded(stripeObject as Invoice)
            "customer.subscription.updated" -> paymentService.onSubscriptionUpdated(stripeObject as Subscription)

            else -> log.info("Unhandled event type: " + event.type)
          }
          ResponseEntity.ok().build()

        } else {
          throw IllegalArgumentException("strip deserialization failed" + event.type)
        }

      } catch (e: Exception) {
        log.error("Webhook error while parsing request.", e)
        ResponseEntity.status(400).build()
      }
    }

}
