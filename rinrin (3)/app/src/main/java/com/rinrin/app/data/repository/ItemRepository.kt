package com.rinrin.app.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: RuntimeException("Task failed"))
        }
    }
}

@Singleton
class ItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun saveItem(item: ItemData): Result<String> {
        return try {
            val collection = firestore.collection("items")
            val docRef = if (item.id.isEmpty()) {
                collection.document()
            } else {
                collection.document(item.id)
            }
            val finalItem = item.copy(id = docRef.id)
            docRef.set(finalItem).awaitTask()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItems(): Result<List<ItemData>> {
        return try {
            val snapshot = firestore.collection("items")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .awaitTask()
            val list = snapshot.toObjects(ItemData::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
