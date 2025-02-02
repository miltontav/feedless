package org.migor.feedless.plan

import com.stripe.model.Card
import com.stripe.model.Customer
import com.stripe.model.Dispute
import com.stripe.model.Invoice
import com.stripe.model.Price
import com.stripe.model.Product
import com.stripe.model.Subscription
import com.stripe.model.checkout.Session
import com.stripe.param.CustomerCreateParams
import com.stripe.param.PriceCreateParams
import com.stripe.param.ProductCreateParams
import com.stripe.param.checkout.SessionCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.common.PropertyService
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.corrId
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class PaymentService {

  private val log = LoggerFactory.getLogger(PaymentService::class.simpleName)

  @Autowired
  private lateinit var orderDAO: OrderDAO

  @Autowired
  private lateinit var invoiceDAO: InvoiceDAO

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var userDAO: UserDAO

  @Transactional
  suspend fun createCheckoutSession(orderId: UUID): Session {
    log.info("[${coroutineContext.corrId()}] createCheckoutSession $orderId")
    val order = withContext(Dispatchers.IO) {
      orderDAO.findByIdWithProduct(orderId) ?: throw NotFoundException("Order $orderId not found")
    }

    val user = withContext(Dispatchers.IO) {
      userDAO.findById(order.userId).orElseThrow()
    }

    val customer = if (user.stripeCustomerId == null) {
      Customer.create(CustomerCreateParams.builder().build())
    } else {
      Customer.retrieve(user.stripeCustomerId)
    }

    val priceParams = PriceCreateParams.builder()
      .setUnitAmount(order.price.toLong())
      .setCurrency("usd")
      .setProduct(resolveProductId(order))
      .setRecurring(
        PriceCreateParams.Recurring.builder()
          .setInterval(PriceCreateParams.Recurring.Interval.YEAR)
          .build()
      )
      .build()

    val price = Price.create(priceParams)

    val host = propertyService.appHost
    val params = SessionCreateParams.builder()
      .setMode(SessionCreateParams.Mode.SUBSCRIPTION) // Mode must be SUBSCRIPTION
      .setCustomer(customer.id)
      .addLineItem(
        SessionCreateParams.LineItem.builder()
          .setQuantity(1)
          .setPrice(price.id)
          .build()
      )
      .setSuccessUrl("$host/success.html?session_id={CHECKOUT_SESSION_ID}")
      .setCancelUrl("$host/cancel.html")
      .build()
    val session = Session.create(params)

    order.stripeSessionId = session.id
    order.stripeSubscriptionId = session.subscription
    withContext(Dispatchers.IO) {
      orderDAO.save(order)
    }

    return session
  }

  private fun resolveProductId(order: OrderEntity): String {
    return if (order.product!!.stripeProductId == null) {
      val productParams = ProductCreateParams.builder()
        .setName(order.product!!.name) // Name of your product
        //        .setDescription("Access to premium features") // Optional
        .build()

      Product.create(productParams).id
    } else {
      order.product!!.stripeProductId!!
    }
  }

  suspend fun onDisputeCreated(dispute: Dispute) {
    log.info("onDisputeCreated $dispute")
  }

  suspend fun onUserDeleted(card: Card) {
    log.info("onUserDeleted $card")
  }

  suspend fun onCustomerDeleted(customer: Customer) {
    log.info("onCustomerDeleted $customer")
  }

  suspend fun onSubscriptionDeleted(subscription: Subscription) {
    log.info("onSubscriptionDeleted $subscription")
  }

  suspend fun onSubscriptionPaused(subscription: Subscription) {
    log.info("onSubscriptionPaused $subscription")
  }

  suspend fun onDisputeSubmitted(dispute: Dispute) {
    log.info("onDisputeSubmitted $dispute")
  }

  @Transactional(readOnly = true)
  suspend fun onInvoiceCreated(invoice: Invoice) {
//    log.info("onInvoiceCreated $invoice")

    val order = withContext(Dispatchers.IO) {
      orderDAO.findByStripeSubscriptionId(invoice.subscription)
    } ?: throw NotFoundException("Order ${invoice.subscription} not found")

    if (order.paidFrom == null) {
      order.paidFrom = invoice.periodStart.toLocalDateTime()
    }

    order.paidFrom = invoice.periodEnd.toLocalDateTime()

    val i = InvoiceEntity()
    i.orderId = order.id
    i.invoiceId = invoice.id
    i.pdfUrl = invoice.invoicePdf
    i.amountPaid = invoice.amountPaid
    i.amountRemaining = invoice.amountRemaining
    i.customerEmail = invoice.customerEmail

    withContext(Dispatchers.IO) {
      invoiceDAO.save(i)
    }
  }

  suspend fun onSubscriptionCreated(subscription: Subscription) {
//    log.info("onSubscriptionCreated $subscription")
  }

  suspend fun onPaymentSucceeded(invoice: Invoice) {
    log.info("onPaymentSucceeded $invoice")

  }

  suspend fun onSubscriptionUpdated(subscription: Subscription) {
//    log.info("onSubscriptionUpdated $subscription")
  }
}
