package com.example.czytaj_etykiety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val apiService = ApiClient.apiService

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchProduct(barcode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _product.value = null

            try {
                val response = apiService.getProductByBarcode(barcode)

                if (response.isSuccessful()) {
                    val productResponse = response.body()
                    if (productResponse?.status == 1 && productResponse.product != null) {
                        _product.value = productResponse.product
                    } else {
                        _error.value = "Produkt nie został znaleziony w bazie danych"
                    }
                } else {
                    _error.value = "Błąd serwera: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd połączenia: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearProduct() {
        _product.value = null
        _error.value = null
    }
}