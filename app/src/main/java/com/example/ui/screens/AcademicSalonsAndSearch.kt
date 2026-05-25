package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.ui.viewmodel.LiteratureViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AcademicSalonsAndSearch(
    viewModel: LiteratureViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val books by viewModel.books.collectAsState()
    val allAnnotations by viewModel.annotations.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchScope by viewModel.searchScope.collectAsState() // "Books", "Annotations"

    var selectedTab by remember { mutableStateOf(0) } // 0 = Search & Citation, 1 = Intellectual Salon

    // Citation builder state
    var selectedBookForCitationId by remember { mutableStateOf<String>("") }
    var citationStyle by remember { mutableStateOf("Chicago Author-Date") } // MLA, APA, Chicago

    // Salon social state
    val salonCircles = listOf(
        "Ancient Hellenistic Dialogue Circle",
        "Elizabethan Drama & Tragedy League",
        "Late Modernism & Woolf Societal Salon",
        "Critical Theory & Sovereignty Seminar"
    )
    var activeCircleIndex by remember { mutableStateOf(0) }
    var showShareExcerptDialog by remember { mutableStateOf(false) }
    var sharedExcerptText by remember { mutableStateOf("") }
    var sharedAnnotationComment by remember { mutableStateOf("") }

    // Mock scholarly community posts
    var mockSalonPosts by remember {
        mutableStateOf(
            listOf(
                SalonPost(
                    author = "Prof. Alistair Vance",
                    institution = "University of Oxford",
                    circle = "Ancient Hellenistic Dialogue Circle",
                    excerpt = "We are made for cooperation, like feet, like hands, like the rows of the upper and lower teeth.",
                    comment = "What is striking here is Aurelius's organic view of absolute statehood. Cooperation isn't merely a strategic contract, but a metabolic necessity.",
                    timestamp = "2 hours ago"
                ),
                SalonPost(
                    author = "Dr. Helen Rostova",
                    institution = "Trinity College Dublin",
                    circle = "Late Modernism & Woolf Societal Salon",
                    excerpt = "A woman must have money and a room of her own if she is to write fiction.",
                    comment = "Note the economic determinism Woolf applies. She forces us to confront how historical lack of property structured female interiority itself.",
                    timestamp = "1 day ago"
                )
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant Tab Bar Header
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Search & Citation Engine", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Intellectual Salon", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // ==========================================
                // TAB 0: SEARCH & CITATION ENGINE BACKEND
                // ==========================================
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search Bar
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "CENTRAL SEARCH COMPASS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    label = { Text("Search keywords / Sovereignty / Stoic...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "") },
                                    modifier = Modifier.fillMaxWidth().testTag("general_search_field")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf("Books", "Annotations").forEach { scope ->
                                        FilterChip(
                                            selected = searchScope == scope,
                                            onClick = { viewModel.setSearchScope(scope) },
                                            label = { Text(scope) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Query Search Output Results
                    if (searchScope == "Books") {
                        val queryResult = books.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.author.contains(searchQuery, ignoreCase = true) ||
                            it.content.contains(searchQuery, ignoreCase = true)
                        }

                        item {
                            Text(
                                text = "FOUND ${queryResult.size} MANUSCRIPTS",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        items(queryResult) { book ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "By ${book.author} — Category: ${book.category}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = book.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        // Annotations Search Scope
                        val queryResult = allAnnotations.filter {
                            it.note.contains(searchQuery, ignoreCase = true) ||
                            it.excerpt.contains(searchQuery, ignoreCase = true) ||
                            it.tags.contains(searchQuery, ignoreCase = true)
                        }

                        item {
                            Text(
                                text = "FOUND ${queryResult.size} SAVED ANNOTATIONS",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        items(queryResult) { annot ->
                            val linkedBook = books.find { it.id == annot.bookId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "\"${annot.excerpt}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = annot.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Source: ${linkedBook?.title ?: "Unknown"} — Class: ${annot.commentType}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }

                    // Separation Line
                    item { Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) }

                    // Academic Citation Builder
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(16.dp)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MenuBook, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Academic Citation Scribe",
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Text(
                                    text = "Generate precise citations referencing your digital archives automatically for thesis usage.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text("Select Manuscript Target", style = MaterialTheme.typography.labelSmall)
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    books.forEach { bk ->
                                        FilterChip(
                                            selected = selectedBookForCitationId == bk.id,
                                            onClick = { selectedBookForCitationId = bk.id },
                                            label = { Text(bk.title, fontSize = 10.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Citation Standard", style = MaterialTheme.typography.labelSmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("MLA (9th)", "APA (7th)", "Chicago Author-Date").forEach { style ->
                                        FilterChip(
                                            selected = citationStyle == style,
                                            onClick = { citationStyle = style },
                                            label = { Text(style, fontSize = 10.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                val targetBk = books.find { it.id == selectedBookForCitationId }
                                if (targetBk != null) {
                                    val compiledCitation = remember(targetBk, citationStyle) {
                                        createCitationString(targetBk, citationStyle)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline)
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = compiledCitation,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Serif,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = android.content.ClipData.newPlainText("Citation", compiledCitation)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "Citation added to Clipboard", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.align(Alignment.End),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "", modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Copy Citation", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        "Please select a standard book cover from above to generate academic bibliography outputs.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // TAB 1: INTELLECTUAL SALON (COMMUNITY CIRCLES)
                // ==========================================
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Circle Selection Panel
                    item {
                        Card {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "ACTIVE SALON SEMINARS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    salonCircles.forEachIndexed { index, circle ->
                                        FilterChip(
                                            selected = activeCircleIndex == index,
                                            onClick = { activeCircleIndex = index },
                                            label = { Text(circle, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVESTREAM DISCUSSION",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { showShareExcerptDialog = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("salon_share_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Post Marginalia", fontSize = 11.sp)
                            }
                        }
                    }

                    // Feed entries filtering by selected Circle
                    val activeCircle = salonCircles[activeCircleIndex]
                    val filteredPosts = mockSalonPosts.filter { it.circle == activeCircle }

                    if (filteredPosts.isNotEmpty()) {
                        items(filteredPosts) { post ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(
                                                text = post.author,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = post.institution,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        Text(
                                            text = post.timestamp,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(48.dp)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "\"${post.excerpt}\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = post.comment,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = "",
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "No current seminar inputs in this Circle",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = FontFamily.Serif
                                    )
                                    Text(
                                        "Be the first scholar to pose a commentary!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareExcerptDialog) {
        val selectableBooks = books
        var selectedLocalAnnotationIndex by remember { mutableStateOf<Int?>(null) }

        AlertDialog(
            onDismissRequest = { showShareExcerptDialog = false },
            title = {
                Text(
                    "Publish Marginal Outline To Salon",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Choose the Academic Circle target:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        salonCircles.forEachIndexed { idx, crcl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeCircleIndex = idx }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = activeCircleIndex == idx,
                                    onClick = { activeCircleIndex = idx }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(crcl, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = sharedExcerptText,
                            onValueChange = { sharedExcerptText = it },
                            label = { Text("Quoted Passage Excerpt") },
                            placeholder = { Text("e.g. Objections become targets...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = sharedAnnotationComment,
                            onValueChange = { sharedAnnotationComment = it },
                            label = { Text("Your scholastic essay/commentary") },
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sharedExcerptText.isNotBlank() && sharedAnnotationComment.isNotBlank()) {
                            val newPost = SalonPost(
                                author = "My Scholar Self",
                                institution = "Private Archives",
                                circle = salonCircles[activeCircleIndex],
                                excerpt = sharedExcerptText,
                                comment = sharedAnnotationComment,
                                timestamp = "Just now"
                            )
                            mockSalonPosts = listOf(newPost) + mockSalonPosts
                            showShareExcerptDialog = false
                            sharedExcerptText = ""
                            sharedAnnotationComment = ""
                        }
                    }
                ) {
                    Text("Publish to Salon")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareExcerptDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class SalonPost(
    val author: String,
    val institution: String,
    val circle: String,
    val excerpt: String,
    val comment: String,
    val timestamp: String
)

/**
 * Creates academic citations based on official MLA, APA, or Chicago standards.
 */
fun createCitationString(book: Book, style: String): String {
    val authorSplit = book.author.split(" ")
    val lastName = authorSplit.lastOrNull() ?: ""
    val firstName = authorSplit.dropLast(1).joinToString(" ").ifBlank { "" }
    val formattedAuthor = if (lastName.isNotBlank() && firstName.isNotBlank()) "$lastName, $firstName" else book.author

    val mockPublisher = "Athenaeum Classical Library"
    val publishedYear = book.year.filter { it.isDigit() }.ifBlank { "1924" }

    return when (style) {
        "APA (7th)" -> {
            "$formattedAuthor. ($publishedYear). ${book.title}. $mockPublisher."
        }
        "MLA (9th)" -> {
            "$formattedAuthor. *${book.title}*. $mockPublisher, $publishedYear."
        }
        else -> {
            "$formattedAuthor. $publishedYear. *${book.title}*. Athens: $mockPublisher."
        }
    }
}
