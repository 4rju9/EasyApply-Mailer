package app.netlify.dev4rju9.easyapplymailer.utils

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

object GmailSender {

    fun sendMail(
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

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(senderEmail))
            message.setRecipients(Message.RecipientType.TO, recipients.joinToString(",") { it })
            message.subject = subject

            val multipart = MimeMultipart()

            val textBodyPart = MimeBodyPart()
            textBodyPart.setText(body)

            val attachmentBodyPart = MimeBodyPart()
            attachmentBodyPart.dataHandler = DataHandler(FileDataSource(resumePath))
            attachmentBodyPart.fileName = resumeName

            multipart.addBodyPart(textBodyPart)
            multipart.addBodyPart(attachmentBodyPart)

            message.setContent(multipart)

            Transport.send(message)

        } catch (e: MessagingException) {
            e.printStackTrace()
            throw RuntimeException("Failed to send email")
        }
    }

}