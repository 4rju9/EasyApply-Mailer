package app.netlify.dev4rju9.easyapplymailer.ui.screens.setup

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SetupScreen (
    viewModel: SetupViewModel = hiltViewModel(),
    onSetupDone: () -> Unit
) {
    val context = LocalContext.current

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPdfSelected(it, context) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) pdfPickerLauncher.launch(arrayOf("application/pdf"))
    }

    LaunchedEffect(viewModel.selectedPdfUri) {
        viewModel.loadPDF(context)
    }

    val pdfPreviewBitmap by viewModel.pdfPreviewBitmap.collectAsState()
    val showHelpDialog = remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold (
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Setup Profile",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineLarge
                    )

                    IconButton (
                        onClick = { showHelpDialog.value = true }
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Password Help", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        viewModel.saveUser(context) { success ->
                            if (success) onSetupDone()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Profile")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceAround
        ) {

            Column {

                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row (
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text("Pick Resume PDF")
                    }

                    if (viewModel.selectedPdfFileName.isNotBlank()) {
                        Button(
                            onClick = {
                                viewModel.selectedPdfUri.let {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(it, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(context, "No app found to view PDF", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Text("Open Selected PDF")
                        }
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                if (viewModel.selectedPdfFileName.isNotBlank()) {
                    Text("Selected PDF: ${viewModel.selectedPdfFileName}", maxLines = 1, color = MaterialTheme.colorScheme.primary)
                }

                if (pdfPreviewBitmap != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AndroidView(
                        factory = { context ->
                            ImageView(context).apply {
                                setImageBitmap(pdfPreviewBitmap)
                                scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                        },
                        update = { imageView ->
                            imageView.setImageBitmap(pdfPreviewBitmap)
                        },
                        modifier = Modifier
                            .wrapContentWidth()
                            .wrapContentHeight()
                            .background(Color.White)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

            }

        }

        if (showHelpDialog.value) {

            AlertDialog(
                onDismissRequest = { showHelpDialog.value = false },
                confirmButton = {
                    Button(onClick = { showHelpDialog.value = false }) {
                        Text("Got it")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myaccount.google.com/apppasswords"))
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No browser found to open the link.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Open app passwords")
                    }
                },
                title = { Text("Guide to\nGet the password") },
                text = {
                    val annotatedText = buildAnnotatedString {
                        append("Don't use your Gmail password. Follow the steps:\n")
                        append("Step 1: Go to ")

                        pushStringAnnotation(tag = "URL", annotation = "https://myaccount.google.com/")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("myaccount.google.com")
                        }
                        pop()

                        append("\nStep 2: Select Security tab\n")
                        append("Step 3: Turn on your 2FA verification\n")
                        append("Step 4: On search bar type 'App Passwords'\n")
                        append("Step 5: Choose a name\n")
                        append("Step 6: You'll see a password window, copy it from there and paste it here")
                    }
                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Unable to open the link", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                textContentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }

}