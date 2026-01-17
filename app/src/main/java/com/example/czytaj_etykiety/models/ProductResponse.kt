package com.example.czytaj_etykiety.models

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("code") val barcode: String,
    @SerializedName("status") val status: Int,
    @SerializedName("product") val product: Product?
)

data class Product(
    @SerializedName("product_name") val name: String?,
    @SerializedName("product_name_pl") val namePl: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("quantity") val quantity: String?,
) {
    fun getDisplayName(): String {
        return namePl ?: name ?: "Nieznany produkt"
    }
}