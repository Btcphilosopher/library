package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Book
import com.example.data.model.Annotation
import com.example.data.model.LiteraryConnection
import com.example.data.model.ReadingJournal

@Database(
    entities = [
        Book::class,
        Annotation::class,
        LiteraryConnection::class,
        ReadingJournal::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun annotationDao(): AnnotationDao
    abstract fun connectionDao(): LiteraryConnectionDao
    abstract fun journalDao(): ReadingJournalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "athenaeum_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
