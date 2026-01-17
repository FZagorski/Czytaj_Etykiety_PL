package com.example.czytaj_etykiety.api

import com.example.czytaj_etykiety.models.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = "code,product_name,product_name_pl,brands,quantity"
    ): Response<ProductResponse>
}