package org.migor.feedless.plan

import com.stripe.model.Card
import com.stripe.model.Customer
import com.stripe.model.Dispute
import com.stripe.model.Event
import com.stripe.model.EventDataObjectDeserializer
import com.stripe.model.Invoice
import com.stripe.model.StripeObject
import com.stripe.model.Subscription
import com.stripe.net.Webhook
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.migor.feedless.common.PropertyService
import org.migor.feedless.repository.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.*

class PaymentControllerTest {

  private lateinit var paymentService: PaymentService
  private lateinit var paymentController: PaymentController
  private lateinit var stripeApiKey: String
  private lateinit var stripeWebhookSecret: String

  @BeforeEach
  fun setUp() {
    stripeApiKey = "--stripeApiKey--"
    stripeWebhookSecret = "--stripeWebhookSecret--"

    paymentService = mock(PaymentService::class.java)

    paymentController = PaymentController(
      mock(OrderService::class.java),
      paymentService,
      mock(PropertyService::class.java),
      stripeApiKey,
      stripeWebhookSecret
    )
  }

  @Test
  fun `handles charge_dispute_created`() = runTest {
    prepare("charge.dispute.created", mock(Dispute::class.java))
    verify(paymentService).onDisputeCreated(any(Dispute::class.java))
  }

  @Test
  fun `handles account_external_account_deleted`() = runTest {
    prepare("account.external_account.deleted", mock(Card::class.java))
    verify(paymentService).onUserDeleted(any(Card::class.java))
  }
  @Test
  fun `handles customer_deleted`() = runTest {
    prepare("customer.deleted", mock(Customer::class.java))
    verify(paymentService).onCustomerDeleted(any(Customer::class.java))
  }
  @Test
  fun `handles customer_subscription_deleted`() = runTest {
    prepare("customer.subscription.deleted", mock(Subscription::class.java))
    verify(paymentService).onSubscriptionDeleted(any(Subscription::class.java))
  }
  @Test
  fun `handles customer_subscription_paused`() = runTest {
    prepare("customer.subscription.paused", mock(Subscription::class.java))
    verify(paymentService).onSubscriptionPaused(any(Subscription::class.java))
  }
  @Test
  fun `handles issuing_dispute_submitted`() = runTest {
    prepare("issuing_dispute.submitted", mock(Dispute::class.java))
    verify(paymentService).onDisputeSubmitted(any(Dispute::class.java))
  }
  @Test
  fun `handles customer_subscription_created`() = runTest {
    prepare("customer.subscription.created", mock(Subscription::class.java))
    verify(paymentService).onSubscriptionCreated(any(Subscription::class.java))
  }
  @Test
  fun `handles invoice_created`() = runTest {
    prepare("invoice.created", mock(Invoice::class.java))
    verify(paymentService).onInvoiceCreated(any(Invoice::class.java))
  }
  @Test
  fun `handles invoice_payment_succeeded`() = runTest {
    prepare("invoice.payment_succeeded", mock(Invoice::class.java))
    verify(paymentService).onPaymentSucceeded(any(Invoice::class.java))
  }
  @Test
  fun `handles customer_subscription_updated`() = runTest {
    prepare("customer.subscription.updated", mock(Subscription::class.java))
    verify(paymentService).onSubscriptionUpdated(any(Subscription::class.java))
  }

  private suspend fun prepare(eventType: String, stripeObject: StripeObject) {
    mockStatic(Webhook::class.java).use { mocked ->

      val dataObjectDeserializer = mock(EventDataObjectDeserializer::class.java)
      `when`(dataObjectDeserializer.`object`).thenReturn(Optional.of(stripeObject))

      val event = mock(Event::class.java)
      `when`(event.dataObjectDeserializer).thenReturn(dataObjectDeserializer)
      `when`(event.type).thenReturn(eventType)
      mocked.`when`<Event> {
        Webhook.constructEvent(
          any(String::class.java),
          any(String::class.java),
          any(String::class.java)
        )
      }.thenReturn(event)

      val httpRequest = mock(HttpServletRequest::class.java)
      `when`(httpRequest.getHeader("stripe-signature")).thenReturn("")

      paymentController.webhook("", httpRequest)
    }
  }
}
