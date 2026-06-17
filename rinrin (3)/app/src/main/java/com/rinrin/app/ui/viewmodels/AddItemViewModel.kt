package com.rinrin.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinrin.app.data.repository.ImageUploadRepository
import com.rinrin.app.data.repository.ItemData
import com.rinrin.app.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val imageUploadRepository: ImageUploadRepository
) : ViewModel() {

    private val _isImageUploading = MutableStateFlow(false)
    val isImageUploading: StateFlow<Boolean> = _isImageUploading.asStateFlow()

    private val _uploadedImageUrl = MutableStateFlow<String?>(null)
    val uploadedImageUrl: StateFlow<String?> = _uploadedImageUrl.asStateFlow()

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    private val _publishSuccess = MutableStateFlow(false)
    val publishSuccess: StateFlow<Boolean> = _publishSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun uploadImage(file: File) {
        viewModelScope.launch {
            _isImageUploading.value = true
            _error.value = null
            val url = imageUploadRepository.uploadImage(file)
            if (url != null) {
                _uploadedImageUrl.value = url
            } else {
                _error.value = "Failed to upload image. Please try again."
            }
            _isImageUploading.value = false
        }
    }

    fun publishItem(item: ItemData) {
        viewModelScope.launch {
            _isPublishing.value = true
            _error.value = null
            val result = itemRepository.saveItem(item)
            if (result.isSuccess) {
                _publishSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to save item"
            }
            _isPublishing.value = false
        }
    }

    fun clearUploadUrl() {
        _uploadedImageUrl.value = null
    }

    fun resetState() {
        _publishSuccess.value = false
        _error.value = null
        _uploadedImageUrl.value = null
        _isImageUploading.value = false
        _isPublishing.value = false
    }
}
