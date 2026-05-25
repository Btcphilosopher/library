package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.db.AppDatabase
import com.example.data.model.Book
import com.example.data.model.Annotation
import com.example.data.model.LiteraryConnection
import com.example.data.model.ReadingJournal
import com.example.data.repository.LiteratureRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class LiteratureViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LiteratureRepository

    // Central Data States
    val books: StateFlow<List<Book>>
    val annotations: StateFlow<List<Annotation>>
    val connections: StateFlow<List<LiteraryConnection>>
    val journals: StateFlow<List<ReadingJournal>>

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchScope = MutableStateFlow("Books") // "Books", "Annotations", "Journals"
    val searchScope: StateFlow<String> = _searchScope.asStateFlow()

    // View Reader State
    private val _activeBookId = MutableStateFlow<String?>(null)
    val activeBook: StateFlow<Book?> = _activeBookId
        .flatMapLatest { id ->
            if (id != null) repository.getBookById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeAnnotations: StateFlow<List<Annotation>> = _activeBookId
        .flatMapLatest { id ->
            if (id != null) repository.getAnnotationsForBook(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customize Reader Preferences
    private val _readerFontSize = MutableStateFlow(18f) // in sp
    val readerFontSize: StateFlow<Float> = _readerFontSize.asStateFlow()

    private val _readerIsSerif = MutableStateFlow(true)
    val readerIsSerif: StateFlow<Boolean> = _readerIsSerif.asStateFlow()

    private val _readerLineSpacing = MutableStateFlow(1.5f)
    val readerLineSpacing: StateFlow<Float> = _readerLineSpacing.asStateFlow()

    private val _readerTheme = MutableStateFlow("Paper") // "Paper", "Charcoal", "Dark Obsidian"
    val readerTheme: StateFlow<String> = _readerTheme.asStateFlow()

    // Interactive Annotating Flow In-Reader
    private val _selectedText = MutableStateFlow("")
    val selectedText: StateFlow<String> = _selectedText.asStateFlow()
    
    private val _isGeneratingAiCommentary = MutableStateFlow(false)
    val isGeneratingAiCommentary: StateFlow<Boolean> = _isGeneratingAiCommentary.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LiteratureRepository(
            database.bookDao(),
            database.annotationDao(),
            database.connectionDao(),
            database.journalDao()
        )

        books = repository.allBooks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        annotations = repository.allAnnotations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        connections = repository.allConnections.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        journals = repository.allJournals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Ensure database has pre-loaded material for superb user experience
        viewModelScope.launch {
            repository.prepopulateDefaultDatabase()
        }
    }

    // Setters for searching
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchScope(scope: String) {
        _searchScope.value = scope
    }

    // Reader state control
    fun selectActiveBook(bookId: String?) {
        _activeBookId.value = bookId
        _selectedText.value = "" // clear on book switch
    }

    fun setHighlightSelection(text: String) {
        _selectedText.value = text
    }

    fun updateReaderFontSize(size: Float) {
        _readerFontSize.value = size.coerceIn(12f, 32f)
    }

    fun setReaderSerif(isSerif: Boolean) {
        _readerIsSerif.value = isSerif
    }

    fun updateReaderLineSpacing(spacing: Float) {
        _readerLineSpacing.value = spacing.coerceIn(1.0f, 2.5f)
    }

    fun setReaderTheme(theme: String) {
        _readerTheme.value = theme
    }

    // Update progress in database
    fun saveProgress(bookId: String, page: Int, totalPages: Int) {
        viewModelScope.launch {
            val progress = (page.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)
            repository.updateBookProgress(bookId, progress, page)
        }
    }

    // Core annotation operations
    fun addAnnotation(
        bookId: String,
        excerpt: String,
        note: String,
        commentType: String,
        tags: String,
        startOffset: Int = 0,
        endOffset: Int = 0
    ) {
        viewModelScope.launch {
            val annot = Annotation(
                bookId = bookId,
                excerpt = excerpt,
                note = note,
                commentType = commentType,
                tags = tags,
                offsetStart = startOffset,
                offsetEnd = endOffset
            )
            repository.insertAnnotation(annot)
            _selectedText.value = "" // reset selection
        }
    }

    fun generateAiAnalysisForHighlight(bookTitle: String, author: String, onComplete: (String, String, String) -> Unit) {
        val selected = _selectedText.value
        if (selected.isBlank()) return

        _isGeneratingAiCommentary.value = true
        viewModelScope.launch {
            val commentary = GeminiClient.generateCommentary(selected, bookTitle, author)
            
            // Extract Type and Tags if returned in bracket format like [Type: Philosophical Commentary] [Tags: Stoicism, Growth]
            var cleanCommentary = commentary
            var type = "Philosophical Commentary"
            var tags = "Stoicism"

            val typePattern = "\\[Type:\\s*(.*?)\\]".toRegex()
            val tagsPattern = "\\[Tags:\\s*(.*?)\\]".toRegex()

            typePattern.find(commentary)?.let {
                type = it.groupValues[1]
                cleanCommentary = cleanCommentary.replace(it.value, "")
            }
            tagsPattern.find(commentary)?.let {
                tags = it.groupValues[1]
                cleanCommentary = cleanCommentary.replace(it.value, "")
            }

            cleanCommentary = cleanCommentary.trim().removePrefix("\n").trim()
            _isGeneratingAiCommentary.value = false
            onComplete(cleanCommentary, type, tags)
        }
    }

    fun removeAnnotation(id: String) {
        viewModelScope.launch {
            repository.deleteAnnotation(id)
        }
    }

    // Literary connections
    fun addConnection(sourceBookId: String, targetBookId: String, sourceConcept: String, targetConcept: String, description: String) {
        viewModelScope.launch {
            val connection = LiteraryConnection(
                sourceBookId = sourceBookId,
                targetBookId = targetBookId,
                sourceConcept = sourceConcept,
                targetConcept = targetConcept,
                description = description
            )
            repository.insertConnection(connection)
        }
    }

    fun removeConnection(id: String) {
        viewModelScope.launch {
            repository.deleteConnection(id)
        }
    }

    // Journal configurations
    fun addJournalEntry(bookId: String?, title: String, body: String) {
        viewModelScope.launch {
            val entry = ReadingJournal(
                bookId = bookId,
                title = title,
                body = body
            )
            repository.insertJournal(entry)
        }
    }

    fun updateJournalEntry(id: String, bookId: String?, title: String, body: String) {
        viewModelScope.launch {
            val entry = ReadingJournal(
                id = id,
                bookId = bookId,
                title = title,
                body = body
            )
            repository.insertJournal(entry)
        }
    }

    fun removeJournalEntry(id: String) {
        viewModelScope.launch {
            repository.deleteJournal(id)
        }
    }

    // Add manual custom books to the shelf
    fun addCustomBook(title: String, author: String, desc: String, category: String, text: String, statsPages: Int) {
        viewModelScope.launch {
            val book = Book(
                title = title,
                author = author,
                description = desc,
                category = category,
                content = text.ifBlank { "Unannotated custom reader sheet." },
                totalPages = statsPages.coerceAtLeast(1)
            )
            repository.insertBook(book)
        }
    }
}
