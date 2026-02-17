package com.example.czytaj_etykiety

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "allergen_preferences")

object AllergensManager {

    private val ALL_ALLERGENS = listOf(
        "en:yeast" to "Drożdże",
        "en:sulphur-dioxide-and-sulphites" to "Dwutlenek siarki i siarczyny",
        "en:msg" to "Glutaminian sodu",
        "en:gluten" to "Gluten",
        "en:eggs" to "Jajka",
        "en:barley" to "Jęczmień",
        "en:corn" to "Kukurydza",
        "en:lupin" to "Łubin",
        "en:molluscs" to "Mięczaki",
        "en:milk" to "Mleko",
        "en:mustard" to "Musztarda",
        "en:nuts" to "Orzechy",
        "en:peanuts" to "Orzeszki ziemne",
        "en:oats" to "Owies",
        "en:wheat" to "Pszenica",
        "en:fish" to "Ryby",
        "en:rice" to "Ryż",
        "en:celery" to "Seler",
        "en:sesame-seeds" to "Sezam",
        "en:sulphites" to "Siarczyny",
        "en:shellfish" to "Skorupiaki",
        "en:soybeans" to "Soja",
        "en:gelatin" to "Żelatyna",
        "en:rye" to "Żyto",
    )

    private val SELECTED_ALLERGENS_KEY = stringSetPreferencesKey("selected_allergens")
    private val ALLERGENS_ENABLED_KEY = booleanPreferencesKey("allergens_enabled")

    fun getAllAllergens(): List<Pair<String, String>> = ALL_ALLERGENS

    suspend fun getSelectedAllergens(context: Context): Set<String> {
        return context.dataStore.data
            .map { preferences ->
                preferences[SELECTED_ALLERGENS_KEY] ?: emptySet()
            }
            .firstOrNull() ?: emptySet()
    }

    suspend fun saveSelectedAllergens(context: Context, allergens: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_ALLERGENS_KEY] = allergens
        }
    }

    fun safetyCheck(productAllergens: List<String>?, userAllergens: Set<String>): ProductSafety {
        if (productAllergens.isNullOrEmpty()) {
            return ProductSafety.UNKNOWN
        }

        val matchedAllergens = productAllergens.filter { allergen ->
            userAllergens.any { userAllergen ->
                allergen.contains(userAllergen, ignoreCase = true)
            }
        }

        return if (matchedAllergens.isEmpty()) {
            ProductSafety.SAFE
        } else {
            ProductSafety.UNSAFE(matchedAllergens)
        }
    }



    sealed class ProductSafety {
        object SAFE : ProductSafety()
        data class UNSAFE(val allergens: List<String>) : ProductSafety()
        object UNKNOWN : ProductSafety()
    }
}