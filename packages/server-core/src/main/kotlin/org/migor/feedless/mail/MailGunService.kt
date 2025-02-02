package org.migor.feedless.mail

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.client.MailgunClient
import com.mailgun.model.message.Message
import com.mailgun.model.message.Message.MessageBuilder
import jakarta.mail.internet.MimeMessage
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.user.UserEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.MailMessage
import org.springframework.stereotype.Service

// see https://github.com/mailgun/mailgun-java
@Service
@ConditionalOnBean(MailgunMessagesApi::class)
class MailGunService: MailGatewayService {

  @Autowired
  lateinit var mailgunMessagesApi: MailgunMessagesApi

  override suspend fun send(domain: String, mailMessage: MailMessage) {
    mailgunMessagesApi.sendMessage(domain, Message.builder().build())
  }

}
