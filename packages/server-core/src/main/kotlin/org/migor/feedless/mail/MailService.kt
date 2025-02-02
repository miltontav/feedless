package org.migor.feedless.mail

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile(AppLayer.service)
class MailService {
  private val log = LoggerFactory.getLogger(MailGatewayService::class.simpleName)

  @Autowired(required = false)
  private lateinit var javaMailSender: JavaMailSender

  @Autowired
  private lateinit var templateService: TemplateService

  @Autowired
  private lateinit var mailGatewayService: MailGatewayService

//  @Deprecated("")
//  private fun send(to: String, body: Email) {
//    val mailMessage = SimpleMailMessage()
//    mailMessage.from = body.from
//    mailMessage.setTo(to)
//    mailMessage.text = body.text
//    mailMessage.subject = body.subject
//
//    javaMailSender.send(mailMessage)
//  }

//  override suspend fun sendWelcomeWaitListMail(user: UserEntity) {
////    sendWelcomeAnyMail(corrId, user, WelcomeWaitListMailTemplate(WelcomeMailParams(user.subscription!!.product!!.partOf!!.name)))
//  }
//
//  override suspend fun sendWelcomePaidMail(user: UserEntity) {
////    sendWelcomeAnyMail(corrId, user, WelcomePaidMailTemplate(WelcomeMailParams(user.subscription!!.product!!.partOf!!.name)))
//  }
//
//  override suspend fun sendWelcomeFreeMail(user: UserEntity) {
////    sendWelcomeAnyMail(corrId, user, WelcomeFreeMailTemplate(WelcomeMailParams(user.subscription!!.product!!.partOf!!.name)))
//  }

  suspend fun sendAuthCode(user: UserEntity, otp: OneTimePasswordEntity, description: String) {
    if (StringUtils.isBlank(user.email)) {
      throw IllegalArgumentException("Email is not defined")
    }
    val corrId = coroutineContext.corrId() ?: "-"
    log.info("[${corrId}] send auth mail ${user.email}")

    val from = "no-reply@feedless.org"
//    val domain = productService.getDomain(user.subscription!!.product!!.partOf!!)
//    val subject = "$domain: Access Code"

    val mailMessage = SimpleMailMessage()
    mailMessage.subject = "Access Code"
    val sdf = DateTimeFormatter.ofPattern("HH:mm")
    val domain = ""

    val params = AuthCodeMailParams(
      domain = domain,
      codeValidUntil = otp.validUntil.format(sdf),
      code = otp.password,
      description = description,
      corrId = corrId,
    )
    mailMessage.text = templateService.renderTemplate(AuthCodeMailTemplate(params))
//
//    send(corrId, from, to = arrayOf(user.email), mailData)
  }

//  suspend fun getNoReplyAddress(product: Vertical): String {
//    return "no-reply@foo.bar"
//  }

  fun askVerifyEmail(user: UserEntity, email: String) {

  }

//  private suspend fun send(from: String, to: Array<String>, mailData: MailData) {
//    val mimeMessage = createMimeMessage()
//    val message = MimeMessageHelper(mimeMessage, true, "UTF-8")
//    message.setFrom(from)
//    message.setTo(to)
//    message.setSubject(mailData.subject)
//    message.setText(mailData.body, true)
//    mailData.attachments.filterTo(ArrayList()) { it: MailAttachment -> it.inline }
//      .forEach { inline -> message.addInline(inline.id, inline.resource) }
//    mailData.attachments.filterTo(ArrayList()) { it: MailAttachment -> !it.inline }
//      .forEach { inline -> message.addAttachment(inline.id, inline.resource) }
//    javaMailSender.send(mimeMessage)
//  }

//  private fun <T> sendWelcomeAnyMail(corrId: String, user: UserEntity, template: FtlTemplate<T>) {
//    log.info("[$corrId] send welcome mail ${user.email} using ${template.templateName}")
//    val product = user.subscription!!.product!!.partOf!!
//
//    val mailData = MailData()
//    mailData.subject = "Welcome to ${productService.getDomain(product)}"
////      val params = WelcomeMailParams(
////        productName = product.name
////      )
//    mailData.body = templateService.renderTemplate(corrId, template)
//    send(corrId, getNoReplyAddress(product), arrayOf(user.email), mailData)
//  }

}
