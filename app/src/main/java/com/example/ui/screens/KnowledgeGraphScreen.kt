package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.data.model.LiteraryConnection
import com.example.ui.viewmodel.LiteratureViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeGraphScreen(
    viewModel: LiteratureViewModel,
    modifier: Modifier = Modifier
) {
    val books by viewModel.books.collectAsState()
    val connections by viewModel.connections.collectAsState()

    var selectedConnectionId by remember { mutableStateOf<String?>(null) }
    var showLinkForm by remember { mutableStateOf(false) }

    // Formulation inputs
    var sourceBookSelectedId by remember { mutableStateOf("") }
    var targetBookSelectedId by remember { mutableStateOf("") }
    var sourceConcept by remember { mutableStateOf("") }
    var targetConcept by remember { mutableStateOf("") }
    var connectionDescription by remember { mutableStateOf("") }

    // Map each book to a fixed coordinate for graph presentation
    val bookCoordinates = remember(books) {
        val coords = mutableMapOf<String, Offset>()
        val count = books.size
        if (count > 0) {
            val radius = 300f
            val centerX = 450f
            val centerY = 450f
            books.forEachIndexed { idx, book ->
                val angle = (2 * Math.PI * idx) / count
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                coords[book.id] = Offset(x, y)
            }
        }
        coords
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (books.size >= 2) {
                        sourceBookSelectedId = books.getOrNull(0)?.id ?: ""
                        targetBookSelectedId = books.getOrNull(1)?.id ?: ""
                        sourceConcept = "Primary Theme"
                        targetConcept = "Corresponding Theme"
                        connectionDescription = ""
                        showLinkForm = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_connection_fab")
            ) {
                Icon(Icons.Default.Share, contentDescription = "Interlink Documents")
            }
        },
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Header Academic Title
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "LITERARY GRAPH",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Intellectual Connections",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "A thematic web mapping mutual influences, critiques, and philosophical overlaps across centuries.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            if (books.size < 2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Must populate at least 2 books to map connections.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
                return@Scaffold
            }

            // Central Canvas Interactive Graph View
            val primaryColor = MaterialTheme.colorScheme.primary
            val outlineColor = MaterialTheme.colorScheme.outline
            val surfaceColor = MaterialTheme.colorScheme.surface

            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .size(900.dp)
                        .drawBehind {
                            val dotColor = primaryColor.copy(alpha = 0.15f)
                            val spacing = 20.dp.toPx()
                            val numX = (size.width / spacing).toInt()
                            val numY = (size.height / spacing).toInt()
                            for (i in 0..numX) {
                                for (j in 0..numY) {
                                    drawCircle(
                                        color = dotColor,
                                        radius = 2.5f,
                                        center = Offset(i * spacing, j * spacing)
                                    )
                                }
                            }
                        }
                        .padding(24.dp)
                ) {
                    // 1. Draw connecting threads
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        connections.forEach { conn ->
                            val startPt = bookCoordinates[conn.sourceBookId]
                            val endPt = bookCoordinates[conn.targetBookId]
                            if (startPt != null && endPt != null) {
                                drawLine(
                                    color = primaryColor.copy(alpha = 0.5f),
                                    start = startPt,
                                    end = endPt,
                                    strokeWidth = 3f
                                )
                            }
                        }
                    }

                    // 2. Draw Book Nodes visually as elegant capsule tags
                    books.forEach { book ->
                        val loc = bookCoordinates[book.id] ?: Offset(450f, 450f)
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (loc.x / 3f).dp - 65.dp,
                                    y = (loc.y / 3f).dp - 21.dp
                                )
                                .width(130.dp)
                                .height(42.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(surfaceColor.copy(alpha = 0.95f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable {
                                    // Highlight link details as selected
                                    val matchLink = connections.find {
                                        it.sourceBookId == book.id || it.targetBookId == book.id
                                    }
                                    if (matchLink != null) {
                                        selectedConnectionId = matchLink.id
                                    }
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                // Dynamic theme dot: alternate sage/terracotta indicator
                                val indicatorColor = if (book.title.hashCode() % 2 == 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.tertiary
                                }
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(indicatorColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = book.title,
                                    fontSize = 9.sp,
                                    lineHeight = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Details Panel of Selected Concept
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    "CONNECTION INDEX",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                val selectedConn = connections.find { it.id == selectedConnectionId } ?: connections.firstOrNull()

                if (selectedConn != null) {
                    val srcBook = books.find { it.id == selectedConn.sourceBookId }
                    val dstBook = books.find { it.id == selectedConn.targetBookId }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${srcBook?.title ?: "Unknown"} ⟷ ${dstBook?.title ?: "Unknown"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.removeConnection(selectedConn.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Thematic Bridges: ${selectedConn.sourceConcept} ⇄ ${selectedConn.targetConcept}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = selectedConn.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No selected connections. Hover/Click map nodes above to examine linkages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showLinkForm) {
        val selectableBooks = books
        AlertDialog(
            onDismissRequest = { showLinkForm = false },
            title = {
                Text(
                    "Inscribe Intellectual Link",
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
                        Text("Source Text Node", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectableBooks.forEach { book ->
                                FilterChip(
                                    selected = sourceBookSelectedId == book.id,
                                    onClick = { sourceBookSelectedId = book.id },
                                    label = { Text(book.title, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                    item {
                        Text("Destination Target Node", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectableBooks.forEach { book ->
                                FilterChip(
                                    selected = targetBookSelectedId == book.id,
                                    onClick = { targetBookSelectedId = book.id },
                                    label = { Text(book.title, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = sourceConcept,
                            onValueChange = { sourceConcept = it },
                            label = { Text("Source Concept/Theme") },
                            placeholder = { Text("e.g. Stoicism") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = targetConcept,
                            onValueChange = { targetConcept = it },
                            label = { Text("Target Concept") },
                            placeholder = { Text("e.g. Finitude") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = connectionDescription,
                            onValueChange = { connectionDescription = it },
                            label = { Text("Connection Commentary Description") },
                            placeholder = { Text("Underline why they link...") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sourceBookSelectedId != targetBookSelectedId && connectionDescription.isNotBlank()) {
                            viewModel.addConnection(
                                sourceBookId = sourceBookSelectedId,
                                targetBookId = targetBookSelectedId,
                                sourceConcept = sourceConcept,
                                targetConcept = targetConcept,
                                description = connectionDescription
                            )
                            showLinkForm = false
                        }
                    }
                ) {
                    Text("Weave Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkForm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
