package app.netlify.dev4rju9.easyapplymailer.utils

import android.content.Context
import app.netlify.dev4rju9.easyapplymailer.utils.Utility.createNotification
import app.netlify.dev4rju9.easyapplymailer.utils.Utility.getNotificationManager
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import kotlin.random.Random

object GmailSender {

    fun sendMail(
        context: Context,
        senderEmail: String,
        senderPassword: String,
        recipients: List<String>,
        subject: String,
        body: String,
        resumePath: String,
        resumeName: String
    ) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, senderPassword)
            }
        })

        recipients.forEach { reciever ->

            val nm = getNotificationManager(context)

            try {
                val message = MimeMessage(session)
                message.setFrom(InternetAddress(senderEmail))
                message.setRecipients(Message.RecipientType.TO, reciever)
                message.subject = subject

                val multipart = MimeMultipart()

                val textBodyPart = MimeBodyPart()
                textBodyPart.setText(body)
                multipart.addBodyPart(textBodyPart)

                if (resumePath.isNotEmpty() && resumeName.isNotEmpty()) {
                    val attachmentBodyPart = MimeBodyPart()
                    attachmentBodyPart.dataHandler = DataHandler(FileDataSource(resumePath))
                    attachmentBodyPart.fileName = resumeName
                    multipart.addBodyPart(attachmentBodyPart)
                }

                message.setContent(multipart)
                Transport.send(message)

                nm.notify(
                    Random.nextInt(),
                    createNotification(
                        context.applicationContext,
                        subject,
                        "Mail to $reciever sent successfully."
                    )
                )

            } catch (e: MessagingException) {
                e.printStackTrace()
                nm.notify(
                    Random.nextInt(),
                    createNotification(
                        context.applicationContext,
                        subject,
                        "Mail to $reciever failed to send.\nWith error: ${e.message}"
                    )
                )
                throw RuntimeException("Failed to send email")
            }

        }
    }

}