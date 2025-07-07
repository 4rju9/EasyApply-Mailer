package app.netlify.dev4rju9.easyapplymailer.ui.screens.setup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.netlify.dev4rju9.easyapplymailer.model.repository.Repository
import app.netlify.dev4rju9.easyapplymailer.model.room.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var selectedPdfUri by mutableStateOf<Uri?>(null)
        private set
    var selectedPdfFileName by mutableStateOf("")
        private set

    var user: UserEntity? = null
        private set

    private val _pdfPreviewBitmap = MutableStateFlow<Bitmap?>(null)
    val pdfPreviewBitmap: StateFlow<Bitmap?> = _pdfPreviewBitmap

    init {
        loadUser()
    }

    fun generatePdfPreview(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = renderFirstPageFromPdf(context, uri)
            _pdfPreviewBitmap.value = bitmap
        }
    }

    fun renderFirstPageFromPdf(context: Context, uri: Uri): Bitmap? {
        return try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            val pdfRenderer = PdfRenderer(fileDescriptor)

            val page = pdfRenderer.openPage(0)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            pdfRenderer.close()
            fileDescriptor.close()

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun onNameChanged(newName: String) { name = newName }
    fun onEmailChanged(newEmail: String) { email = newEmail }
    fun onPasswordChanged(newPassword: String) { password = newPassword }

    private fun loadUser() {
        viewModelScope.launch {
            user = repository.getUsers().firstOrNull()
            user?.also {
                if (selectedPdfUri == null) {
                    name = it.name
                    email = it.email
                    password = it.password
                    selectedPdfUri = Uri.parse(it.resumeUri)
                    selectedPdfFileName = it.resumeFileName
                }
            }
        }
    }

    fun loadPDF (context: Context) {
        selectedPdfUri?.also { generatePdfPreview(context, it) }
    }

    fun onPdfSelected(uri: Uri, context: Context) {
        selectedPdfUri = uri
        selectedPdfFileName = extractFileName(uri, context)
        generatePdfPreview(context, uri)
    }

    private fun extractFileName(uri: Uri, context: Context): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                return it.getString(nameIndex) ?: "Unknown"
            }
        }
        return "Unknown"
    }

    fun saveUser(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || password.isBlank() || selectedPdfUri == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    onResult(false)
                }
                return@launch
            }

            withContext(Dispatchers.IO) {
                val user = user?.copy(
                    name = name,
                    email = email,
                    password = password,
                    resumeFileName = selectedPdfFileName,
                    resumeUri = selectedPdfUri.toString()
                ) ?: UserEntity(
                    name = name,
                    email = email,
                    password = password,
                    resumeFileName = selectedPdfFileName,
                    resumeUri = selectedPdfUri.toString()
                )
                repository.saveUser(user)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "User saved successfully", Toast.LENGTH_SHORT).show()
                resetFields()
                onResult(true)
            }
        }
    }

    private fun resetFields() {
        name = ""
        email = ""
        password = ""
        selectedPdfUri = null
        selectedPdfFileName = ""
    }

}