package org.migor.feedless.plan

import com.stripe.Stripe
import jakarta.annotation.PostConstruct
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class StripeConfig {

  @Value("\${app.stripeSecretKey}")
  lateinit var stripeApiKey: String


  @PostConstruct
  fun init() {
    Stripe.apiKey = stripeApiKey
  }
}
