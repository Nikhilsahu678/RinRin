package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ItemData
import com.example.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<ItemData>>(emptyList())
    val items: StateFlow<List<ItemData>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchItems()
    }

    fun fetchItems() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = itemRepository.getItems()
            if (result.isSuccess) {
                _items.value = result.getOrNull() ?: emptyList()
            } else {
                _items.value = emptyList()
            }
            _isLoading.value = false
        }
    }
}
