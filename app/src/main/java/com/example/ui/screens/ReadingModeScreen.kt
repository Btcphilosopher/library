package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.data.model.Annotation
import com.example.ui.viewmodel.LiteratureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingModeScreen(
    viewModel: LiteratureViewModel,
    modifier: Modifier = Modifier
) {
    val activeBook by viewModel.activeBook.collectAsState()
    val activeAnnotations by viewModel.activeAnnotations.collectAsState()

    val fontSize by viewModel.readerFontSize.collectAsState()
    val isSerif by viewModel.readerIsSerif.collectAsState()
    val lineSpacing by viewModel.readerLineSpacing.collectAsState()
    val readerTheme by viewModel.readerTheme.collectAsState()

    val selectedText by viewModel.selectedText.collectAsState()
    val isGeneratingAi by viewModel.isGeneratingAiCommentary.collectAsState()

    var showControlPanel by remember { mutableStateOf(false) }
    var selectedParagraphIndex by remember { mutableStateOf<Int?>(null) }

    // Dialogue for custom annotation
    var showAnnotateDialog by remember { mutableStateOf(false) }
    var customNoteText by remember { mutableStateOf("") }
    var commentType by remember { mutableStateOf("Philosophical Commentary") }
    var tagsInput by remember { mutableStateOf("") }

    if (activeBook == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.Book,
                    contentDescription = "",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Manuscript Selected",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Select a classic work from your library shelves to enter deep reading mode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val book = activeBook!!
    // Split book content into neat readable paragraphs
    val paragraphs = remember(book.content) {
        book.content.split("\n\n").filter { it.isNotBlank() }
    }

    // Adapt background depending on theme
    val readerBg = when (readerTheme) {
        "Charcoal" -> Color(0xFF1D1F21)
        "Dark Obsidian" -> Color(0xFF0F1011)
        else -> Color(0xFFF5F2ED) // Sand Natural Oatmeal Bg
    }
    val readerTextCol = when (readerTheme) {
        "Charcoal" -> Color(0xFFE2DFD2)
        "Dark Obsidian" -> Color(0xFFC7C3B5)
        else -> Color(0xFF2C2621) // Natural Tones Charcoal Text
    }

    val readerFontFamily = if (isSerif) FontFamily.Serif else FontFamily.SansSerif

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(readerBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Immersive Header Book Binder Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectActiveBook(null) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = readerTextCol)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = readerTextCol.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        color = readerTextCol
                    )
                }
                IconButton(onClick = { showControlPanel = !showControlPanel }) {
                    Icon(Icons.Default.Tune, contentDescription = "Typography Adjustments", tint = readerTextCol)
                }
            }

            Divider(color = readerTextCol.copy(alpha = 0.15f))

            // Readable Content Panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy((12 * lineSpacing).dp)
                ) {
                    itemsIndexed(paragraphs) { index, paragraph ->
                        val isSelected = selectedParagraphIndex == index
                        val isAnnotated = activeAnnotations.any { paragraph.contains(it.excerpt) }

                        val pBg = when {
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            isAnnotated -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            else -> Color.Transparent
                        }

                        val pBorderModifier = if (isSelected) {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        } else Modifier

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(pBg)
                                .then(pBorderModifier)
                                .clickable {
                                    if (selectedParagraphIndex == index) {
                                        selectedParagraphIndex = null
                                        viewModel.setHighlightSelection("")
                                    } else {
                                        selectedParagraphIndex = index
                                        viewModel.setHighlightSelection(paragraph.trim())
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = paragraph,
                                    fontSize = fontSize.sp,
                                    fontFamily = readerFontFamily,
                                    color = readerTextCol,
                                    lineHeight = (fontSize * lineSpacing).sp
                                )

                                // Show linked highlights underneath paragraph if present
                                val paragraphAnnotations = activeAnnotations.filter { paragraph.contains(it.excerpt) }
                                if (paragraphAnnotations.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    paragraphAnnotations.forEach { annot ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(40.dp)
                                                    .background(MaterialTheme.colorScheme.tertiary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Marginalia (${annot.commentType})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = annot.note,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontStyle = FontStyle.Italic,
                                                    color = readerTextCol.copy(alpha = 0.8f)
                                                )
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = { viewModel.removeAnnotation(annot.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete Annotation",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Mini toolbar when paragraph selection is active
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(
                        visible = selectedText.isNotBlank(),
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Paragraph Selected",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                // AI commentary trigger
                                Button(
                                    onClick = {
                                        viewModel.generateAiAnalysisForHighlight(book.title, book.author) { response, type, tags ->
                                            customNoteText = response
                                            commentType = type
                                            tagsInput = tags
                                            showAnnotateDialog = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    enabled = !isGeneratingAi,
                                    modifier = Modifier.testTag("ai_marginalia_button")
                                ) {
                                    if (isGeneratingAi) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = MaterialTheme.colorScheme.onTertiary, strokeWidth = 1.5.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mentoring...", fontSize = 11.sp)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = "", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AI Marginalia", fontSize = 11.sp)
                                    }
                                }

                                // Manual highlight trigger
                                Button(
                                    onClick = {
                                        customNoteText = ""
                                        commentType = "Philosophical Commentary"
                                        tagsInput = ""
                                        showAnnotateDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Bookmark, contentDescription = "", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Highlight", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Simple Reader Footer Page progress indicator
            Divider(color = readerTextCol.copy(alpha = 0.15f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ARCHIVAL ID: ${book.id.take(8).uppercase()}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = readerTextCol.copy(alpha = 0.6f)
                )

                // Simulated next previous physical pages
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (book.currentPage > 1) {
                                viewModel.saveProgress(book.id, book.currentPage - 1, book.totalPages)
                                selectedParagraphIndex = null
                                viewModel.setHighlightSelection("")
                            }
                        },
                        enabled = book.currentPage > 1
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = readerTextCol)
                    }
                    Text(
                        text = "Page ${book.currentPage} of ${book.totalPages}",
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp,
                        color = readerTextCol
                    )
                    IconButton(
                        onClick = {
                            if (book.currentPage < book.totalPages) {
                                viewModel.saveProgress(book.id, book.currentPage + 1, book.totalPages)
                                selectedParagraphIndex = null
                                viewModel.setHighlightSelection("")
                            }
                        },
                        enabled = book.currentPage < book.totalPages
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = readerTextCol)
                    }
                }
            }
        }

        // Drop-down floating panel for typography control
        AnimatedVisibility(
            visible = showControlPanel,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Typography Settings",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showControlPanel = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Font Family Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Type Style", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.setReaderSerif(true) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSerif) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text("Serif Letter")
                            }
                            FilledTonalButton(
                                onClick = { viewModel.setReaderSerif(false) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (!isSerif) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text("Sans Modern")
                            }
                        }
                    }

                    // Font Size Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Font Size", style = MaterialTheme.typography.bodyMedium)
                            Text("${fontSize.toInt()} sp", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = fontSize,
                            onValueChange = { viewModel.updateReaderFontSize(it) },
                            valueRange = 12f..32f,
                            steps = 10
                        )
                    }

                    // Line Spacing Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reading Density", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1.2f, 1.5f, 1.8f).forEach { scale ->
                                FilterChip(
                                    selected = lineSpacing == scale,
                                    onClick = { viewModel.updateReaderLineSpacing(scale) },
                                    label = { Text("${scale}x") }
                                )
                            }
                        }
                    }

                    // Palette / Reading Background Theme
                    Column {
                        Text("Academic Atmosphere", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Paper", "Charcoal", "Dark Obsidian").forEach { themeName ->
                                FilterChip(
                                    selected = readerTheme == themeName,
                                    onClick = { viewModel.setReaderTheme(themeName) },
                                    label = { Text(themeName) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to save annotation
    if (showAnnotateDialog) {
        AlertDialog(
            onDismissRequest = { showAnnotateDialog = false },
            title = {
                Text(
                    "Marginal Commentary",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "\"$selectedText\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = customNoteText,
                        onValueChange = { customNoteText = it },
                        label = { Text("Your scholastic notes / insights") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Marginalia Class", style = MaterialTheme.typography.labelSmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            listOf("Philosophical Commentary", "Historical Note", "Cross-Reference", "Footnote").forEach { type ->
                                FilterChip(
                                    selected = commentType == type,
                                    onClick = { commentType = type },
                                    label = { Text(type, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = { Text("Thematic tags (comma separated)") },
                        placeholder = { Text("Stoicism, Sovereign, Empire") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addAnnotation(
                            bookId = book.id,
                            excerpt = selectedText,
                            note = customNoteText.ifBlank { "Unannotated highlighted study coordinate." },
                            commentType = commentType,
                            tags = tagsInput
                        )
                        showAnnotateDialog = false
                        selectedParagraphIndex = null
                    }
                ) {
                    Text("Inscribe")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAnnotateDialog = false }) {
                    Text("Discard")
                }
            }
        )
    }
}
