package com.example.pocketlibrary

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class MyLibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val bookDao = AppDatabase.getDatabase(application).bookDao()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private val firebaseRepository = FirebaseRepository()

    init {
        syncFirestoreToRoom()
    }

    private fun syncFirestoreToRoom() {
        viewModelScope.launch {
            firebaseRepository.signInAnonymously()
            val firestoreBooks = firebaseRepository.fetchBooksFromFirestore()
            firestoreBooks.forEach { book ->
                bookDao.insertBook(book)
            }
        }
    }

    val books: StateFlow<List<BookEntity>> = _searchQuery
        .transformLatest { query ->
            if (query.isBlank()) {
                emitAll(bookDao.getAllBooks())
            } else {
                emitAll(bookDao.searchBooks("%$query%"))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            bookDao.deleteBook(book)
        }
    }

    fun addBookManually(title: String, author: String, year: String) {
        if (title.isBlank() || author.isBlank()) return
        viewModelScope.launch {
            val newBook = BookEntity(
                apiKey = "manual_${UUID.randomUUID()}",
                title = title,
                author = author,
                publishYear = year.toIntOrNull(),
                coverUrl = null
            )
            bookDao.insertBook(newBook)
            firebaseRepository.saveBookToFirestore(newBook)
        }
    }

    fun updateBookCover(book: BookEntity, newCoverBitmap: Bitmap) {
        viewModelScope.launch {
            val newCoverPath = saveBitmapToInternalStorage(newCoverBitmap)
            if (newCoverPath != null) {
                val updatedBook = book.copy(coverUrl = newCoverPath)
                bookDao.insertBook(updatedBook)
                firebaseRepository.saveBookToFirestore(updatedBook)
            }
        }
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String? {
        val context = getApplication<Application>().applicationContext
        val file = File(context.filesDir, "${UUID.randomUUID()}.jpg")
        return try {
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return MyLibraryViewModel(application) as T
            }
        }
    }
}