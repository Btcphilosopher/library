package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.ui.viewmodel.LiteratureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryDashboardScreen(
    viewModel: LiteratureViewModel,
    onBookSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val books by viewModel.books.collectAsState()
    val annotations by viewModel.annotations.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newAuthor by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Philosophy") }
    var newDesc by remember { mutableStateOf("") }
    var newText by remember { mutableStateOf("") }
    var newPages by remember { mutableStateOf("12") }

    val categories = listOf("Philosophy", "Classics", "Fiction", "Research", "Journals")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_custom_book_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Import Literature")
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Editorial Header Card
            item {
                AcademicBanner(
                    totalBooks = books.size,
                    totalAnnotations = annotations.size
                )
            }

            // Currently Reading Shelf
            val currentlyReading = books.filter { it.progress > 0.0f && it.progress < 1.0f }
            if (currentlyReading.isNotEmpty()) {
                item {
                    SectionHeader(title = "Currently Reading", subtitle = "Your active intellectual loops")
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentlyReading) { book ->
                            ActiveBookCard(book = book, onClick = { onBookSelected(book.id) })
                        }
                    }
                }
            }

            // Bookshelf by Category
            categories.forEach { category ->
                val catBooks = books.filter { it.category.equals(category, ignoreCase = true) }
                if (catBooks.isNotEmpty()) {
                    item {
                        SectionHeader(title = category, subtitle = "Curated archival selections")
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(catBooks) { book ->
                                BookshelfCard(book = book, onClick = { onBookSelected(book.id) })
                            }
                        }
                    }
                }
            }

            // Empty state placeholder instructions if no books remain
            if (books.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryBooks,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "The Bookshelf is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Click the '+' button to import ancient text or customized commentary sheets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal dialogue to import new text documents
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    "Catalog New Document",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newAuthor,
                            onValueChange = { newAuthor = it },
                            label = { Text("Author / Origin") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Category", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.take(3).forEach { cat ->
                                FilterChip(
                                    selected = newCategory == cat,
                                    onClick = { newCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.drop(3).forEach { cat ->
                                FilterChip(
                                    selected = newCategory == cat,
                                    onClick = { newCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = newDesc,
                            onValueChange = { newDesc = it },
                            label = { Text("Historical/Analytical Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newText,
                            onValueChange = { newText = it },
                            label = { Text("Manuscript Body Text") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = newPages,
                            onValueChange = { newPages = it },
                            label = { Text("Estimated Pages") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.addCustomBook(
                                title = newTitle,
                                author = newAuthor.ifBlank { "Anonymous" },
                                desc = newDesc,
                                category = newCategory,
                                text = newText,
                                statsPages = newPages.toIntOrNull() ?: 10
                            )
                            newTitle = ""
                            newAuthor = ""
                            newDesc = ""
                            newText = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add to Shelves")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AcademicBanner(totalBooks: Int, totalAnnotations: Int) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "THE PRIVATE COLLECTION OF",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Codex Archive",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            // Initials Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AM",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Stats Card (Natural Tones styled)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MANUSCRIPTS",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$totalBooks Inscribed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MARGINALIA NOTES",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$totalAnnotations Collected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun ActiveBookCard(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp)
            .clickable(onClick = onClick)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Retro spine binder layout
            Box(
                modifier = Modifier
                    .width(68.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2C2621))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        // Tiny custom progress bar on cover
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(book.progress)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = book.author.uppercase(),
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.secondary,
                        fontStyle = FontStyle.Italic
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Page ${book.currentPage} / ${book.totalPages}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(book.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { book.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun BookshelfCard(book: Book, onClick: () -> Unit) {
    val coverBg = when (book.category.lowercase()) {
        "philosophy" -> MaterialTheme.colorScheme.primary
        "classics" -> MaterialTheme.colorScheme.tertiary
        "journals" -> Color(0xFF6E685E)
        "research" -> Color(0xFF434E48)
        else -> Color(0xFF2C2621)
    }

    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Book leather bound graphical card
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(170.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(coverBg)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(10.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Golden ivory bind highlight
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0xFFEAE7DC).copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    if (book.year.isNotBlank()) {
                        Text(
                            text = book.year,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = book.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = book.author,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
