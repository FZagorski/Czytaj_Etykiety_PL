package com.example.czytaj_etykiety

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllergensViewModel : ViewModel() {

    private val _selectedAllergens = MutableStateFlow<Set<String>>(emptySet())
    val selectedAllergens: StateFlow<Set<String>> = _selectedAllergens.asStateFlow()

    private val _isAllergenCheckEnabled = MutableStateFlow(false)
    val isAllergenCheckEnabled: StateFlow<Boolean> = _isAllergenCheckEnabled.asStateFlow()

    fun loadUserPreferences(context: Context) {
        viewModelScope.launch {
            _selectedAllergens.value = AllergensManager.getSelectedAllergens(context)
        }
    }

    private fun updateSelectedAllergens(context: Context, allergens: Set<String>) {
        viewModelScope.launch {
            AllergensManager.saveSelectedAllergens(context, allergens)
            _selectedAllergens.value = allergens
        }
    }

    fun toggleAllergen(allergenCode: String, context: Context) {
        viewModelScope.launch {
            val current = _selectedAllergens.value.toMutableSet()
            if (current.contains(allergenCode)) {
                current.remove(allergenCode)
            } else {
                current.add(allergenCode)
            }
            updateSelectedAllergens(context, current)
        }
    }

    fun checkProductSafety(productAllergens: List<String>?): AllergensManager.ProductSafety {
        return AllergensManager.safetyCheck(productAllergens, _selectedAllergens.value)
    }
}
