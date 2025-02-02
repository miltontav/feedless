package org.migor.feedless.mail

import org.springframework.mail.MailMessage

interface MailGatewayService {

  suspend fun send(domain: String, mailMessage: MailMessage)

}
