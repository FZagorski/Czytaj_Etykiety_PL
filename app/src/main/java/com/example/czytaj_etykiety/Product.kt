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
    @SerializedName("nutriments") val nutriments: Map<String, Double>?,
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

    fun getKcal(): Double? {
        return nutriments?.get("energy-kcal_100g")
            ?: nutriments?.get("energy-kcal")
            ?: nutriments?.get("energy_100g")?.let { it / 4.184 }
    }

    fun getKcalText(): String {
        val kcal = getKcal()
        return if (kcal != null) {
            "${kcal.toInt()} kcal / 100g"
        } else {
            "Brak danych"
        }
    }
    fun isFromPoland(): Boolean {
        if (countries.isNullOrBlank()) return false
        val polishKeywords = listOf(
            "Poland",
            "Polska",
            "PL",
            "pl",
            "Poland,",
            "Polska,"
        )
        return polishKeywords.any { keyword ->
            countries?.contains(keyword, ignoreCase = true) == true
        }
    }
}


