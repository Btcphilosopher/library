package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReadingJournal
import com.example.ui.viewmodel.LiteratureViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingJournalScreen(
    viewModel: LiteratureViewModel,
    modifier: Modifier = Modifier
) {
    val journals by viewModel.journals.collectAsState()
    val books by viewModel.books.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedJournalForEdit by remember { mutableStateOf<ReadingJournal?>(null) }

    var journalTitle by remember { mutableStateOf("") }
    var journalBody by remember { mutableStateOf("") }
    var selectedBookIdForLink by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    journalTitle = ""
                    journalBody = ""
                    selectedBookIdForLink = null
                    selectedJournalForEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_journal_fab")
            ) {
                Icon(Icons.Default.Edit, contentDescription = "New Log")
            }
        },
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Journal Header Description
            item {
                Column {
                    Text(
                        text = "STUDY LOGS & REVIEWS",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "The Reading Journal",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "A space for daily reflection, drafting thesis statements, and monitoring your private intellectual development.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            item {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            }

            // Journal Timeline Items list
            items(journals) { journal ->
                val linkedBook = books.find { it.id == journal.bookId }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedJournalForEdit = journal
                            journalTitle = journal.title
                            journalBody = journal.body
                            selectedBookIdForLink = journal.bookId
                            showAddDialog = true
                        }
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = dateFormat.format(Date(journal.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            IconButton(
                                onClick = { viewModel.removeJournalEntry(journal.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = journal.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (linkedBook != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = "Book Ref",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "In reference to: ${linkedBook.title} by ${linkedBook.author}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = journal.body,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }

            // Journal Empty State
            if (journals.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Reflections Written Yet",
                            fontFamily = FontFamily.Serif,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Inscribe your very first thoughts by taping the pencil button.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }

    // Dialogue to Add or Modify reflective logs
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    if (selectedJournalForEdit == null) "Record Reflection" else "Amend Reflection",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = journalTitle,
                        onValueChange = { journalTitle = it },
                        label = { Text("Reflection Title/Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Associate Literature Link", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedBookIdForLink == null,
                                onClick = { selectedBookIdForLink = null },
                                label = { Text("General Log") }
                            )
                            books.forEach { bk ->
                                FilterChip(
                                    selected = selectedBookIdForLink == bk.id,
                                    onClick = { selectedBookIdForLink = bk.id },
                                    label = { Text(bk.title, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = journalBody,
                        onValueChange = { journalBody = it },
                        label = { Text("Reflective Manuscript Body") },
                        minLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (journalTitle.isNotBlank() && journalBody.isNotBlank()) {
                            if (selectedJournalForEdit == null) {
                                viewModel.addJournalEntry(
                                    bookId = selectedBookIdForLink,
                                    title = journalTitle,
                                    body = journalBody
                                )
                            } else {
                                viewModel.updateJournalEntry(
                                    id = selectedJournalForEdit!!.id,
                                    bookId = selectedBookIdForLink,
                                    title = journalTitle,
                                    body = journalBody
                                )
                            }
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(if (selectedJournalForEdit == null) "Record" else "Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Discard")
                }
            }
        )
    }
}
