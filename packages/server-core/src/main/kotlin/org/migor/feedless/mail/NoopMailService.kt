package org.migor.feedless.mail

import jakarta.mail.internet.MimeMessage
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.user.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Profile
import org.springframework.mail.MailMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("!${AppProfiles.mail} & ${AppLayer.service}")
@ConditionalOnBean(MailGatewayService::class)
class NoopMailService : MailGatewayService {
  private val log = LoggerFactory.getLogger(NoopMailService::class.simpleName)

  override suspend fun send(domain: String, mailMessage: MailMessage) {
    log.info(mailMessage.toString())
  }


}
