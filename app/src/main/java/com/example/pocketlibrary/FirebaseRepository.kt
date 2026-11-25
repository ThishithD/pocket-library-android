package com.example.pocketlibrary

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    suspend fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    fun saveBookToFirestore(book: BookEntity) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("books")
            .add(book)
    }

    suspend fun fetchBooksFromFirestore(): List<BookEntity> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(userId).collection("books").get().await()
            snapshot.toObjects(BookEntity::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}