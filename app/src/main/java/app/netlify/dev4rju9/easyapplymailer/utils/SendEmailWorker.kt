package app.netlify.dev4rju9.easyapplymailer.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SendEmailWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val senderEmail = inputData.getString("senderEmail") ?: return Result.failure()
            val senderPassword = inputData.getString("senderPassword") ?: return Result.failure()
            val subject = inputData.getString("subject") ?: ""
            val body = inputData.getString("body") ?: ""
            val recipients = inputData.getStringArray("recipients")?.toList() ?: emptyList()
            val resumePath = inputData.getString("resumePath")
            val resumeName = inputData.getString("resumeName")

            if (resumePath.isNullOrEmpty() || resumeName.isNullOrEmpty()) {
                return Result.failure(workDataOf("error" to "Resume file not found"))
            }

            GmailSender.sendMail(
                senderEmail = senderEmail,
                senderPassword = senderPassword,
                recipients = recipients,
                subject = subject,
                body = body,
                resumePath = resumePath,
                resumeName = resumeName
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(workDataOf("error" to e.message))
        }
    }
}