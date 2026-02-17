package com.example.czytaj_etykiety

import com.google.gson.annotations.SerializedName


data class Product(
    @SerializedName("code") val barcode: String,
    @SerializedName("status") val status: Int,
    @SerializedName("product") val product: Product?,
    @SerializedName("product_name") val name: String?,
    @SerializedName("product_name_pl") val namePl: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("quantity") val quantity: String?,
    @SerializedName("ingredients_text") val ingredients: String?,
    @SerializedName("ingredients_text_pl") val ingredientsPl: String?,
    @SerializedName("allergens_tags") val allergens: List<String>?,
    @SerializedName("allergens") val allergensText: String?,
    @SerializedName("countries") val countries: String?
) {
    fun getDisplayName(): String {
        return if (!namePl.isNullOrBlank()) namePl.trim()
        else if (!name.isNullOrBlank()) name.trim()
        else "Nieznany produkt"
    }

    fun getAllergensDisplayText(): String {
        return allergens?.joinToString(", ") ?: allergensText ?: "Brak informacji"
    }

    fun getDisplayIngredients(): String {
        return if (!ingredientsPl.isNullOrBlank()) {
            ingredientsPl.trim()
        } else if (!ingredients.isNullOrBlank()) {
            ingredients.trim()
        } else {
            "Brak informacji o składnikach"
        }
    }
}

