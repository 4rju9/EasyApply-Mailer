package app.netlify.dev4rju9.easyapplymailer.ui.screens.addemail

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.netlify.dev4rju9.easyapplymailer.model.repository.Repository
import app.netlify.dev4rju9.easyapplymailer.model.room.EmailEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddEmailViewModel @Inject constructor(
    private val emailRepository: Repository
) : ViewModel() {

    var subject by mutableStateOf("")
        private set
    var body by mutableStateOf("")
        private set

    var email: EmailEntity? = null

    fun onSubjectChanged(newSubject: String) { subject = newSubject }
    fun onBodyChanged(newBody: String) { body = newBody }

    fun loadEmail(email: EmailEntity?) {
        this.email = email
        subject = email?.subject ?: ""
        body = email?.body ?: ""
    }

    fun saveEmail(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (subject.isBlank() || body.isBlank()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    onResult(false)
                }
                return@launch
            }

            withContext(Dispatchers.IO) {
                emailRepository.saveEmail(email?.copy(subject = subject, body = body) ?: EmailEntity(subject = subject, body = body))
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Email template saved successfully", Toast.LENGTH_SHORT).show()
                subject = ""
                body = ""
                onResult(true)
            }
        }
    }

}