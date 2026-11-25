package com.example.pocketlibrary

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class SearchUiState(
    val query: String = "",
    val books: List<BookDoc> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val savedBookApiKeys: Set<String> = emptySet()
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage

    private val bookDao = AppDatabase.getDatabase(application).bookDao()
    private val firebaseRepository = FirebaseRepository()

    init {
        viewModelScope.launch {
            bookDao.getAllBookApiKeys().collect { keys ->
                _uiState.update { it.copy(savedBookApiKeys = keys.toSet()) }
            }
        }
    }

    fun updateQuery(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
    }

    fun searchBooks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = Network.apiService.searchBooks(query = _uiState.value.query)
                _uiState.value = _uiState.value.copy(
                    books = response.docs,
                    isLoading = false
                )
            } catch (e: Exception) {
                val errorMessage = if (e is java.net.UnknownHostException) {
                    "No internet connection. Please try again."
                } else {
                    "An unexpected error occurred."
                }
                _uiState.value = _uiState.value.copy(
                    error = errorMessage,
                    isLoading = false
                )
            }
        }
    }

    fun toggleFavoriteStatus(book: BookDoc, customCover: Any?) {
        viewModelScope.launch {
            val isCurrentlySaved = _uiState.value.savedBookApiKeys.contains(book.key)
            if (isCurrentlySaved) {
                bookDao.deleteBookByApiKey(book.key)
                _snackbarMessage.emit("'${book.title}' removed from library.")
            } else {
                val finalCoverUrl = when (customCover) {
                    is Bitmap -> saveBitmapToInternalStorage(customCover)
                    is String -> customCover
                    else -> book.coverImageUrl
                }

                val bookEntity = BookEntity(
                    apiKey = book.key,
                    title = book.title,
                    author = book.author,
                    publishYear = book.firstPublishYear,
                    coverUrl = finalCoverUrl
                )
                bookDao.insertBook(bookEntity)
                firebaseRepository.saveBookToFirestore(bookEntity)
                _snackbarMessage.emit("'${book.title}' saved to library!")
            }
        }
    }

    fun updateBookCoverInLibrary(book: BookDoc, newCoverBitmap: Bitmap) {
        viewModelScope.launch {
            val newCoverPath = saveBitmapToInternalStorage(newCoverBitmap)
            if (newCoverPath != null) {
                val updatedBook = BookEntity(
                    apiKey = book.key,
                    title = book.title,
                    author = book.author,
                    publishYear = book.firstPublishYear,
                    coverUrl = newCoverPath
                )
                bookDao.insertBook(updatedBook)
                firebaseRepository.saveBookToFirestore(updatedBook)
                _snackbarMessage.emit("'${book.title}' cover updated.")
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
        val Factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val application = checkNotNull(extras[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return SearchViewModel(application) as T
            }
        }
    }
}