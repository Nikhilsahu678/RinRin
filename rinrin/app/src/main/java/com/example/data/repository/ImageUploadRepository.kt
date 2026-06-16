package com.example.data.repository

import android.util.Log
import com.example.data.remote.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ImageUploadRepository {
    private val apiKey = "9e6aef2f7edc70126987cb5efbf1b91d"

    suspend fun uploadImage(file: File): String? {
        return try {
            Log.d("ImageUploadRepository", "Starting upload for file: ${file.absolutePath}, size: ${file.length()}")
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            val response = RetrofitInstance.apiService.uploadImage(apiKey, body)
            if (response.success && response.data != null) {
                Log.d("ImageUploadRepository", "Upload success! Display URL: ${response.data.displayURL}")
                response.data.displayURL
            } else {
                Log.e("ImageUploadRepository", "Upload not success. Response success = ${response.success}")
                null
            }
        } catch (e: Exception) {
            Log.e("ImageUploadRepository", "Exception during image upload", e)
            null
        }
    }
}
