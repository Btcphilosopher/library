package com.example.data.db

import androidx.room.*
import com.example.data.model.Book
import com.example.data.model.Annotation
import com.example.data.model.LiteraryConnection
import com.example.data.model.ReadingJournal
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun getBookById(id: String): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookByIdSync(id: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Query("UPDATE books SET progress = :progress, currentPage = :currentPage WHERE id = :id")
    suspend fun updateReadingProgress(id: String, progress: Float, currentPage: Int)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: String)
}

@Dao
interface AnnotationDao {
    @Query("SELECT * FROM annotations ORDER BY timestamp DESC")
    fun getAllAnnotations(): Flow<List<Annotation>>

    @Query("SELECT * FROM annotations WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getAnnotationsForBook(bookId: String): Flow<List<Annotation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: Annotation)

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteAnnotationById(id: String)
}

@Dao
interface LiteraryConnectionDao {
    @Query("SELECT * FROM literary_connections ORDER BY timestamp DESC")
    fun getAllConnections(): Flow<List<LiteraryConnection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: LiteraryConnection)

    @Query("DELETE FROM literary_connections WHERE id = :id")
    suspend fun deleteConnectionById(id: String)
}

@Dao
interface ReadingJournalDao {
    @Query("SELECT * FROM reading_journals ORDER BY timestamp DESC")
    fun getAllJournals(): Flow<List<ReadingJournal>>

    @Query("SELECT * FROM reading_journals WHERE id = :id LIMIT 1")
    fun getJournalById(id: String): Flow<ReadingJournal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: ReadingJournal)

    @Query("DELETE FROM reading_journals WHERE id = :id")
    suspend fun deleteJournalById(id: String)
}
