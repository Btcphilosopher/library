package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LiteratureViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: LiteratureViewModel = viewModel()
                MainNavigationContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationContainer(viewModel: LiteratureViewModel) {
    var selectedScreen by remember { mutableStateOf("Library") } // Library, Reader, Graph, Journal, Salon
    val activeBook by viewModel.activeBook.collectAsState()

    // Automatically navigate to 'Reader' when a book is clicked
    LaunchedEffect(activeBook) {
        if (activeBook != null) {
            selectedScreen = "Reader"
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                // 1. Library Dashboard Tab
                NavigationBarItem(
                    selected = selectedScreen == "Library",
                    onClick = { selectedScreen = "Library" },
                    icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "Holdings") },
                    label = { Text("Holdings") },
                    modifier = Modifier.testTag("nav_library_tab")
                )

                // 2. Immersive Reader Tab
                NavigationBarItem(
                    selected = selectedScreen == "Reader",
                    onClick = { selectedScreen = "Reader" },
                    icon = { 
                        BadgedBox(badge = {
                            if (activeBook != null) {
                                Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                                    Text("Active", color = MaterialTheme.colorScheme.onTertiary, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }) {
                            Icon(Icons.Default.AutoStories, contentDescription = "Reader")
                        }
                    },
                    label = { Text("Reader") },
                    modifier = Modifier.testTag("nav_reader_tab")
                )

                // 3. Knowledge Graph Tab
                NavigationBarItem(
                    selected = selectedScreen == "Graph",
                    onClick = { selectedScreen = "Graph" },
                    icon = { Icon(Icons.Default.Hub, contentDescription = "Graph Map") },
                    label = { Text("Connections") },
                    modifier = Modifier.testTag("nav_graph_tab")
                )

                // 4. Reading Journal Tab
                NavigationBarItem(
                    selected = selectedScreen == "Journal",
                    onClick = { selectedScreen = "Journal" },
                    icon = { Icon(Icons.Default.Create, contentDescription = "Reading Logs") },
                    label = { Text("Journal") },
                    modifier = Modifier.testTag("nav_journal_tab")
                )

                // 5. Academic Forums Tab
                NavigationBarItem(
                    selected = selectedScreen == "Salon",
                    onClick = { selectedScreen = "Salon" },
                    icon = { Icon(Icons.Default.School, contentDescription = "Academic Salon") },
                    label = { Text("Salon") },
                    modifier = Modifier.testTag("nav_salon_tab")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen contents cross-fade transition
            AnimatedContent(
                targetState = selectedScreen,
                transitionSpec = {
                    (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                },
                label = "ScreenTransition"
            ) { target ->
                when (target) {
                    "Library" -> LibraryDashboardScreen(
                        viewModel = viewModel,
                        onBookSelected = { bookId ->
                            viewModel.selectActiveBook(bookId)
                        },
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    )
                    "Reader" -> ReadingModeScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    )
                    "Graph" -> KnowledgeGraphScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    )
                    "Journal" -> ReadingJournalScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    )
                    "Salon" -> AcademicSalonsAndSearch(
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    )
                }
            }
        }
    }
}
