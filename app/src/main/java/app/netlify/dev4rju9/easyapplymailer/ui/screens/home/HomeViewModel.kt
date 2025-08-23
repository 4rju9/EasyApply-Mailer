package app.netlify.dev4rju9.easyapplymailer.ui.screens.home

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.netlify.dev4rju9.easyapplymailer.model.repository.Repository
import app.netlify.dev4rju9.easyapplymailer.model.room.EmailEntity
import app.netlify.dev4rju9.easyapplymailer.model.room.UserEntity
import app.netlify.dev4rju9.easyapplymailer.utils.SendEmailWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    val emails: StateFlow<List<EmailEntity>> = repository.getEmails()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    lateinit var user: UserEntity
        private set

    init {
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            user = repository.getUsers()[0]
            _userName.value = user.name
        }
    }

    fun sendEmail(email: EmailEntity, recipients: List<String>, context: Context, onSuccess: () -> Unit) {
        if (user == null) {
            Toast.makeText(context, "User profile not found", Toast.LENGTH_SHORT).show()
            return
        }

        val file = getFileFromUri(context, user.resumeUri)
        if (file == null) {
            Toast.makeText(context, "Resume file not found", Toast.LENGTH_SHORT).show()
            return
        }

        _isSending.value = true

        val workData = workDataOf(
            "senderEmail" to user.email,
            "senderPassword" to user.password,
            "recipients" to recipients.toTypedArray(),
            "subject" to email.subject,
            "body" to email.body,
            "resumePath" to file.absolutePath,
            "resumeName" to user.resumeFileName
        )

        val workRequest = OneTimeWorkRequestBuilder<SendEmailWorker>()
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(workRequest.id)
            .observeForever { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    _isSending.value = false
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        Toast.makeText(context, "Email sent successfully", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    } else {
                        val errorMsg = workInfo.outputData.getString("error") ?: "Unknown error"
                        Toast.makeText(context, "Failed to send email: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun getFileFromUri(context: Context, uriString: String): File? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "resume.pdf")
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteEmail(email: EmailEntity, onResult: () -> Unit) = CoroutineScope(Dispatchers.IO).launch {
        repository.deleteEmail(email)
        withContext(Dispatchers.Main) {
            onResult()
        }
    }

}