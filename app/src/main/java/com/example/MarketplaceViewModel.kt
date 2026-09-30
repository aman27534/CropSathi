package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.*

// ── Data models ───────────────────────────────────────────────────────────────

data class FarmerListing(
    val id: String = "",
    val crop: String = "",
    val quantityKg: Double = 0.0,
    val pricePerKg: Double = 0.0,
    val locationLat: Double = 22.7196,   // Default: Indore
    val locationLng: Double = 75.8577,
    val locationName: String = "",
    val farmerName: String = "",
    val farmerPhone: String = "",
    val status: String = "active",        // "active" | "sold"
    val timestampMs: Long = 0L,
    val distanceKm: Double = Double.MAX_VALUE  // computed client-side
)

data class MarketplaceUiState(
    val listings: List<FarmerListing> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isPosting: Boolean = false,
    val postSuccess: Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore

    // User location for distance sorting — hardcoded to Indore for demo
    // (roadmap: location picker or GPS later)
    private val USER_LAT = 22.7196
    private val USER_LNG = 75.8577

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    private val _selectedCropFilter = MutableStateFlow<String?>(null)
    val selectedCropFilter: StateFlow<String?> = _selectedCropFilter.asStateFlow()

    // All available crops (populated from Firestore, shown in filter dropdown)
    private val _availableCrops = MutableStateFlow<List<String>>(emptyList())
    val availableCrops: StateFlow<List<String>> = _availableCrops.asStateFlow()

    init {
        observeListings()
        seedDummyDataIfEmpty()
    }

    // ── Real-time Firestore stream ────────────────────────────────────────────
    private fun observeListings() {
        viewModelScope.launch {
            try {
                var query: Query = db.collection("farmer_listings")
                    .whereEqualTo("status", "active")
                    .orderBy("timestampMs", Query.Direction.DESCENDING)

                query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load listings: ${error.message}"
                        )
                        return@addSnapshotListener
                    }

                    val listings = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            FarmerListing(
                                id = doc.id,
                                crop = doc.getString("crop") ?: "",
                                quantityKg = doc.getDouble("quantityKg") ?: 0.0,
                                pricePerKg = doc.getDouble("pricePerKg") ?: 0.0,
                                locationLat = doc.getDouble("locationLat") ?: USER_LAT,
                                locationLng = doc.getDouble("locationLng") ?: USER_LNG,
                                locationName = doc.getString("locationName") ?: "",
                                farmerName = doc.getString("farmerName") ?: "",
                                farmerPhone = doc.getString("farmerPhone") ?: "",
                                status = doc.getString("status") ?: "active",
                                timestampMs = doc.getLong("timestampMs") ?: 0L
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    // Compute distances and sort nearest-first
                    val withDistances = listings.map { listing ->
                        listing.copy(
                            distanceKm = haversineKm(
                                USER_LAT, USER_LNG,
                                listing.locationLat, listing.locationLng
                            )
                        )
                    }

                    // Apply crop filter if active
                    val filtered = filterByCrop(withDistances, _selectedCropFilter.value)

                    // Update available crops for filter UI
                    val crops = listings.map { it.crop }.distinct().sorted()
                    _availableCrops.value = crops

                    _uiState.value = _uiState.value.copy(
                        listings = filtered,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Firestore error: ${e.message}"
                )
            }
        }
    }

    // ── Filter ────────────────────────────────────────────────────────────────
    fun setFilter(crop: String?) {
        _selectedCropFilter.value = crop
        val current = _uiState.value.listings
        // Re-filter from current listings
        viewModelScope.launch {
            try {
                var query: Query = db.collection("farmer_listings")
                    .whereEqualTo("status", "active")
                if (crop != null) {
                    query = query.whereEqualTo("crop", crop)
                }
                val snapshot = query.get().await()
                val listings = snapshot.documents.mapNotNull { doc ->
                    try {
                        FarmerListing(
                            id = doc.id,
                            crop = doc.getString("crop") ?: "",
                            quantityKg = doc.getDouble("quantityKg") ?: 0.0,
                            pricePerKg = doc.getDouble("pricePerKg") ?: 0.0,
                            locationLat = doc.getDouble("locationLat") ?: USER_LAT,
                            locationLng = doc.getDouble("locationLng") ?: USER_LNG,
                            locationName = doc.getString("locationName") ?: "",
                            farmerName = doc.getString("farmerName") ?: "",
                            farmerPhone = doc.getString("farmerPhone") ?: "",
                            status = doc.getString("status") ?: "active",
                            timestampMs = doc.getLong("timestampMs") ?: 0L
                        ).copy(
                            distanceKm = haversineKm(
                                USER_LAT, USER_LNG,
                                doc.getDouble("locationLat") ?: USER_LAT,
                                doc.getDouble("locationLng") ?: USER_LNG
                            )
                        )
                    } catch (e: Exception) { null }
                }.sortedBy { it.distanceKm }

                _uiState.value = _uiState.value.copy(listings = listings)
            } catch (e: Exception) { /* use existing listings */ }
        }
    }

    // ── Post a new listing ────────────────────────────────────────────────────
    fun postListing(
        crop: String,
        quantityKg: Double,
        pricePerKg: Double,
        locationLat: Double,
        locationLng: Double,
        locationName: String,
        farmerName: String,
        farmerPhone: String
    ) {
        if (crop.isBlank() || farmerName.isBlank() || farmerPhone.isBlank()) return

        _uiState.value = _uiState.value.copy(isPosting = true, postSuccess = false)

        val data = hashMapOf(
            "crop"         to crop,
            "quantityKg"   to quantityKg,
            "pricePerKg"   to pricePerKg,
            "locationLat"  to locationLat,
            "locationLng"  to locationLng,
            "locationName" to locationName,
            "farmerName"   to farmerName,
            "farmerPhone"  to farmerPhone,
            "status"       to "active",
            "timestampMs"  to System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                db.collection("farmer_listings").add(data).await()
                _uiState.value = _uiState.value.copy(isPosting = false, postSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPosting = false,
                    errorMessage = "Failed to post listing: ${e.message}"
                )
            }
        }
    }

    fun markAsSold(listingId: String) {
        viewModelScope.launch {
            try {
                db.collection("farmer_listings").document(listingId)
                    .update("status", "sold").await()
            } catch (_: Exception) {}
        }
    }

    fun clearPostSuccess() {
        _uiState.value = _uiState.value.copy(postSuccess = false)
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private fun filterByCrop(listings: List<FarmerListing>, crop: String?): List<FarmerListing> {
        val filtered = if (crop == null) listings else listings.filter { it.crop == crop }
        return filtered.sortedBy { it.distanceKm }
    }

    /**
     * Haversine formula for straight-line distance (km) between two lat/lng points.
     * Matches the "nearest-first sort" described in the roadmap.
     */
    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371.0  // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    // ── Seed dummy data (only if collection is empty) ─────────────────────────
    private fun seedDummyDataIfEmpty() {
        viewModelScope.launch {
            try {
                val count = db.collection("farmer_listings")
                    .whereEqualTo("status", "active")
                    .limit(1).get().await().size()
                if (count == 0) {
                    val dummyListings = listOf(
                        mapOf("crop" to "Soybean", "quantityKg" to 500.0, "pricePerKg" to 45.0, "locationLat" to 22.7196, "locationLng" to 75.8577, "locationName" to "Indore", "farmerName" to "Ramesh Patidar", "farmerPhone" to "9876543210", "status" to "active", "timestampMs" to System.currentTimeMillis()),
                        mapOf("crop" to "Wheat", "quantityKg" to 1200.0, "pricePerKg" to 22.0, "locationLat" to 23.1765, "locationLng" to 77.4361, "locationName" to "Bhopal", "farmerName" to "Suresh Yadav", "farmerPhone" to "9765432109", "status" to "active", "timestampMs" to System.currentTimeMillis() - 3600000),
                        mapOf("crop" to "Tomato", "quantityKg" to 300.0, "pricePerKg" to 18.0, "locationLat" to 23.1793, "locationLng" to 75.7849, "locationName" to "Dewas", "farmerName" to "Kamlesh Malviya", "farmerPhone" to "9654321098", "status" to "active", "timestampMs" to System.currentTimeMillis() - 7200000),
                        mapOf("crop" to "Onion", "quantityKg" to 2000.0, "pricePerKg" to 12.0, "locationLat" to 22.6825, "locationLng" to 75.3017, "locationName" to "Ratlam", "farmerName" to "Dinesh Chouhan", "farmerPhone" to "9543210987", "status" to "active", "timestampMs" to System.currentTimeMillis() - 10800000),
                        mapOf("crop" to "Soybean", "quantityKg" to 750.0, "pricePerKg" to 47.0, "locationLat" to 23.5106, "locationLng" to 77.0011, "locationName" to "Sehore", "farmerName" to "Govind Sharma", "farmerPhone" to "9432109876", "status" to "active", "timestampMs" to System.currentTimeMillis() - 14400000),
                        mapOf("crop" to "Potato", "quantityKg" to 400.0, "pricePerKg" to 15.0, "locationLat" to 24.5854, "locationLng" to 73.7125, "locationName" to "Ujjain", "farmerName" to "Prakash Verma", "farmerPhone" to "9321098765", "status" to "active", "timestampMs" to System.currentTimeMillis() - 18000000),
                        mapOf("crop" to "Chickpea", "quantityKg" to 600.0, "pricePerKg" to 65.0, "locationLat" to 24.0932, "locationLng" to 77.3196, "locationName" to "Vidisha", "farmerName" to "Satish Rajput", "farmerPhone" to "9210987654", "status" to "active", "timestampMs" to System.currentTimeMillis() - 21600000),
                        mapOf("crop" to "Wheat", "quantityKg" to 800.0, "pricePerKg" to 21.0, "locationLat" to 25.4358, "locationLng" to 78.5941, "locationName" to "Sagar", "farmerName" to "Mohan Tiwari", "farmerPhone" to "9109876543", "status" to "active", "timestampMs" to System.currentTimeMillis() - 25200000),
                        mapOf("crop" to "Maize", "quantityKg" to 1500.0, "pricePerKg" to 19.0, "locationLat" to 22.3072, "locationLng" to 73.1812, "locationName" to "Khandwa", "farmerName" to "Vijay Prajapati", "farmerPhone" to "9098765432", "status" to "active", "timestampMs" to System.currentTimeMillis() - 28800000),
                        mapOf("crop" to "Tomato", "quantityKg" to 200.0, "pricePerKg" to 20.0, "locationLat" to 22.9676, "locationLng" to 78.7379, "locationName" to "Narsinghpur", "farmerName" to "Ashok Patel", "farmerPhone" to "9987654321", "status" to "active", "timestampMs" to System.currentTimeMillis() - 32400000),
                        mapOf("crop" to "Mustard", "quantityKg" to 300.0, "pricePerKg" to 55.0, "locationLat" to 23.8388, "locationLng" to 78.7378, "locationName" to "Damoh", "farmerName" to "Hemlata Sahu", "farmerPhone" to "9876012345", "status" to "active", "timestampMs" to System.currentTimeMillis() - 36000000),
                        mapOf("crop" to "Onion", "quantityKg" to 3000.0, "pricePerKg" to 10.0, "locationLat" to 23.6024, "locationLng" to 75.0791, "locationName" to "Mandsaur", "farmerName" to "Rajendra Gurjar", "farmerPhone" to "9765012345", "status" to "active", "timestampMs" to System.currentTimeMillis() - 39600000),
                        mapOf("crop" to "Garlic", "quantityKg" to 250.0, "pricePerKg" to 120.0, "locationLat" to 23.7196, "locationLng" to 74.9167, "locationName" to "Neemuch", "farmerName" to "Balram Kirar", "farmerPhone" to "9654012345", "status" to "active", "timestampMs" to System.currentTimeMillis() - 43200000),
                        mapOf("crop" to "Soybean", "quantityKg" to 1000.0, "pricePerKg" to 44.0, "locationLat" to 22.0074, "locationLng" to 76.0529, "locationName" to "Harda", "farmerName" to "Lallu Kahar", "farmerPhone" to "9543012345", "status" to "active", "timestampMs" to System.currentTimeMillis() - 46800000),
                        mapOf("crop" to "Chickpea", "quantityKg" to 900.0, "pricePerKg" to 62.0, "locationLat" to 26.2183, "locationLng" to 78.1828, "locationName" to "Gwalior", "farmerName" to "Meena Kushwaha", "farmerPhone" to "9432012345", "status" to "active", "timestampMs" to System.currentTimeMillis() - 50400000)
                    )
                    val batch = db.batch()
                    dummyListings.forEach { data ->
                        val ref = db.collection("farmer_listings").document()
                        batch.set(ref, data)
                    }
                    batch.commit().await()
                }
            } catch (_: Exception) { /* seed failure is non-critical */ }
        }
    }
}
