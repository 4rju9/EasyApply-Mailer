package app.netlify.dev4rju9.easyapplymailer.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.app.NotificationCompat
import app.netlify.dev4rju9.easyapplymailer.MainActivity
import app.netlify.dev4rju9.easyapplymailer.R
import java.util.Calendar

object Utility {

    fun extractPlaceholdersCount(body: String): Int {
        val regex = "\\[.*?]".toRegex()
        return regex.findAll(body).count()
    }

    fun replacePlaceholders(body: String, replacements: List<String>): String {
        var result = body
        val regex = "\\[.*?]".toRegex()
        val iterator = replacements.iterator()

        result = regex.replace(result) {
            if (iterator.hasNext()) iterator.next() else "[placeholder]"
        }
        return result
    }

    fun getGreeting(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Fix your sleep cycle"
        }
    }

    fun replacePlaceholdersWithHighlights(
        template: String,
        replacements: List<String>,
        placeholderPattern: Regex = Regex("\\[.*?]"),
        highlightColor: Color
    ): AnnotatedString {
        val result = buildAnnotatedString {
            var lastIndex = 0
            var replacementIndex = 0

            for (match in placeholderPattern.findAll(template)) {
                val start = match.range.first
                val end = match.range.last + 1

                append(template.substring(lastIndex, start))

                if (replacementIndex < replacements.size) {
                    withStyle(style = SpanStyle(color = highlightColor)) {
                        append(replacements[replacementIndex])
                    }
                    replacementIndex++
                }

                lastIndex = end
            }

            if (lastIndex < template.length) {
                append(template.substring(lastIndex))
            }
        }

        return result
    }

    fun getNotificationManager (applicationContext: Context) = applicationContext
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotification(
        applicationContext: Context,
        title: String,
        body: String
    ): Notification {

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, "easy_apply_mailer")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()
    }

}