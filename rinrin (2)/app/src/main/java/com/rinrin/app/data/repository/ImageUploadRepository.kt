package com.rinrin.app.data.repository

import android.util.Log
import com.rinrin.app.data.remote.ImgbbApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUploadRepository @Inject constructor(
    private val apiService: ImgbbApiService
) {
    private val apiKey = "9e6aef2f7edc70126987cb5efbf1b91d"

    suspend fun uploadImage(file: File): String? {
        return try {
            Log.d("ImageUploadRepository", "Starting upload for file: ${file.absolutePath}, size: ${file.length()}")
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            val response = apiService.uploadImage(apiKey, body)
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
