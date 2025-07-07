package app.netlify.dev4rju9.easyapplymailer.ui.screens.home

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.netlify.dev4rju9.easyapplymailer.model.repository.Repository
import app.netlify.dev4rju9.easyapplymailer.model.room.EmailEntity
import app.netlify.dev4rju9.easyapplymailer.model.room.UserEntity
import app.netlify.dev4rju9.easyapplymailer.utils.GmailSender
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
        viewModelScope.launch(Dispatchers.IO) {
            _isSending.value = true
            try {
                if (user != null) {
                    val file = getFileFromUri(context, user.resumeUri)
                    if (file == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Resume file not found", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    GmailSender.sendMail(
                        senderEmail = user.email,
                        senderPassword = user.password,
                        recipients = recipients,
                        subject = email.subject,
                        body = email.body,
                        resumePath = file.absolutePath,
                        resumeName = user.resumeFileName
                    )

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Email sent successfully", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "User profile not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to send email: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                _isSending.value = false
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