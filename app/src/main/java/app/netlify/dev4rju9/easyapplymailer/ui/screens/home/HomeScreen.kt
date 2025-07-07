package app.netlify.dev4rju9.easyapplymailer.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.netlify.dev4rju9.easyapplymailer.model.room.EmailEntity
import app.netlify.dev4rju9.easyapplymailer.utils.Utility.extractPlaceholdersCount
import app.netlify.dev4rju9.easyapplymailer.utils.Utility.replacePlaceholders
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSetup: () -> Unit,
    onNavigateToAddEmail: (EmailEntity?) -> Unit
) {
    val context = LocalContext.current
    val emails by viewModel.emails.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val greeting = getGreeting()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddEmail(null) },
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Email")
            }
        },
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Hey, $userName!",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Text(
                            text = greeting,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IconButton(onClick = { onNavigateToSetup() }) {
                        Icon(Icons.Default.Person, contentDescription = "Settings")
                    }
                }
            }
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        containerColor = MaterialTheme.colorScheme.background,
        content = { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {

                items(emails) { email ->
                    EmailCard(
                        email = email,
                        isSending = isSending,
                        onEditClick = { onNavigateToAddEmail(email) },
                        onSendClick = { recipients, body, onSuccess ->
                            if (recipients.isNotBlank()) {
                                viewModel.sendEmail(email.copy(body = body), recipients.split(",").map { it.trim() }, context, onSuccess)
                            } else {
                                Toast.makeText(context, "Please enter recipients", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDeleteClick = {
                            viewModel.deleteEmail(email) {
                                Toast.makeText(context, "Email deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun EmailCard(
    email: EmailEntity,
    isSending: Boolean,
    onEditClick: () -> Unit,
    onSendClick: (String, String, () -> Unit) -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var recipientInput by remember { mutableStateOf("") }
    val recipientList = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    val replacements = remember { mutableStateListOf<String>() }
    var replacementInput by remember { mutableStateOf("") }
    val placeholderCount = extractPlaceholdersCount(email.body)
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(16.dp)
        ) {
            Text(
                text = email.subject,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (replacements.size == placeholderCount && expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = replacePlaceholdersWithHighlights(
                        template = email.body,
                        replacements = replacements,
                        highlightColor = MaterialTheme.colorScheme.primary
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            } else {
                Text(
                    text = if (!expanded) email.body.take(200) else email.body,
                    maxLines = if (!expanded) 5 else Int.MAX_VALUE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                if (placeholderCount > 0) {
                    OutlinedTextField(
                        value = replacementInput,
                        onValueChange = {
                            replacementInput = it
                            if (it.contains(",")) {
                                val newItems = it.split(",").map { s -> s.trim() }.filter { it.isNotEmpty() }
                                if (replacements.size + newItems.size <= placeholderCount) {
                                    replacements.addAll(newItems)
                                    replacementInput = ""
                                } else {
                                    Toast.makeText(context, "You have already filled all placeholders.", Toast.LENGTH_SHORT).show()
                                    replacementInput = ""
                                }
                            }
                        },
                        label = { Text("Replacements (comma-separated)") },
                        trailingIcon = {
                            if (replacementInput.isNotBlank()) {
                                IconButton(onClick = {
                                    val newItems = replacementInput.split(",").map { s -> s.trim() }.filter { it.isNotEmpty() }
                                    if (replacements.size + newItems.size <= placeholderCount) {
                                        replacements.addAll(newItems)
                                        replacementInput = ""
                                    } else {
                                        Toast.makeText(context, "You have already filled all placeholders.", Toast.LENGTH_SHORT).show()
                                        replacementInput = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Add Replacement")
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )

                    if (replacements.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(replacements) { item ->
                                AssistChip(
                                    onClick = { replacements.remove(item) },
                                    label = { Text(item) }
                                )
                            }
                        }

                        LaunchedEffect(recipientList.size) {
                            if (replacements.isNotEmpty()) {
                                listState.animateScrollToItem(replacements.size - 1)
                            }
                        }

                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = recipientInput,
                    onValueChange = {
                        recipientInput = it
                        if (it.contains(",")) {
                            val tags = it.split(",")
                                .map { tag -> tag.trim() }
                                .filter { tag -> tag.isNotEmpty() }
                            recipientList.addAll(tags)
                            recipientInput = ""
                        }
                    },
                    label = { Text("Recipients (comma-separated)") },
                    trailingIcon = {
                        if (recipientInput.isNotBlank()) {
                            IconButton(onClick = {
                                val tags = recipientInput.split(",")
                                    .map { tag -> tag.trim() }
                                    .filter { tag -> tag.isNotEmpty() }
                                recipientList.addAll(tags)
                                recipientInput = ""
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Add Recipient")
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )

                if (recipientList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recipientList) { recipient ->
                            AssistChip(
                                onClick = { recipientList.remove(recipient) },
                                label = { Text(recipient) }
                            )
                        }
                    }

                    LaunchedEffect(recipientList.size) {
                        if (recipientList.isNotEmpty()) {
                            listState.animateScrollToItem(recipientList.size - 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onEditClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(0.2f)
                    ) {
                        Text("Edit")
                    }

                    Spacer(modifier = Modifier.weight(0.05f))

                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(0.2f)
                    ) {
                        Text("Delete")
                    }

                    Spacer(modifier = Modifier.weight(0.05f))

                    Button(
                        onClick = {
                            if (replacements.size < placeholderCount) {
                                Toast.makeText(context, "Please add all replacement values.", Toast.LENGTH_SHORT).show()
                                return@Button
                            } else {
                                val modifiedBody = replacePlaceholders(email.body, replacements)
                                onSendClick(recipientList.joinToString(","), modifiedBody) {
                                    replacements.clear()
                                    recipientList.clear()
                                    recipientInput = ""
                                    replacementInput = ""
                                }
                            }
                        },
                        enabled = !isSending,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(0.25f)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Send Mail")
                        }
                    }
                }
            }
        }
    }
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