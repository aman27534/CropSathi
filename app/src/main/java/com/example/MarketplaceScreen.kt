package com.example

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ── Crop icon helper ──────────────────────────────────────────────────────────
private fun cropColour(crop: String): Color = when (crop.lowercase()) {
    "soybean"  -> Color(0xFF558B2F)
    "wheat"    -> Color(0xFFF9A825)
    "tomato"   -> Color(0xFFC62828)
    "potato"   -> Color(0xFF795548)
    "onion"    -> Color(0xFFAD1457)
    "chickpea" -> Color(0xFF6D4C41)
    "maize"    -> Color(0xFFFFA000)
    "mustard"  -> Color(0xFFEF6C00)
    "garlic"   -> Color(0xFF7B1FA2)
    else       -> Color(0xFF1565C0)
}

private fun distanceLabel(km: Double): String = when {
    km < 1.0   -> "< 1 km"
    km < 10.0  -> "${km.toInt()} km"
    km < 100.0 -> "${km.toInt()} km"
    else       -> "${(km / 100).toInt() * 100}+ km"
}

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen() {
    val context = LocalContext.current
    val vm: MarketplaceViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )

    val uiState         by vm.uiState.collectAsStateWithLifecycle()
    val selectedFilter  by vm.selectedCropFilter.collectAsStateWithLifecycle()
    val availableCrops  by vm.availableCrops.collectAsStateWithLifecycle()

    var showPostSheet   by remember { mutableStateOf(false) }

    // Success snackbar
    LaunchedEffect(uiState.postSuccess) {
        if (uiState.postSuccess) {
            kotlinx.coroutines.delay(2500)
            vm.clearPostSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {

            // ── Top header ────────────────────────────────────────────────────
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.StoreMallDirectory, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("KISAN MARKETPLACE", style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                        Spacer(Modifier.weight(1f))
                        // Listing count badge
                        Surface(shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer) {
                            Text("${uiState.listings.size} active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Text("Sorted by distance from Indore",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(12.dp))

                    // ── Crop filter chips ───────────────────────────────────────
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedFilter == null,
                                onClick = { vm.setFilter(null) },
                                label = { Text("All Crops") },
                                leadingIcon = if (selectedFilter == null) {
                                    { Icon(Icons.Filled.Check, contentDescription = null,
                                        modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                        items(availableCrops) { crop ->
                            FilterChip(
                                selected = selectedFilter == crop,
                                onClick = {
                                    vm.setFilter(if (selectedFilter == crop) null else crop)
                                },
                                label = { Text(crop) },
                                leadingIcon = if (selectedFilter == crop) {
                                    { Icon(Icons.Filled.Check, contentDescription = null,
                                        modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // ── Listings ──────────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text("Loading listings...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                uiState.listings.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)) {
                            Icon(Icons.Filled.Inventory2, contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Spacer(Modifier.height(16.dp))
                            Text("No listings found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (selectedFilter != null) {
                                Spacer(Modifier.height(8.dp))
                                Text("Try removing the crop filter",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))
                                TextButton(onClick = { vm.setFilter(null) }) {
                                    Text("Clear filter")
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.listings, key = { it.id }) { listing ->
                            ListingCard(
                                listing = listing,
                                onContact = {
                                    val intent = Intent(Intent.ACTION_DIAL,
                                        Uri.parse("tel:${listing.farmerPhone}"))
                                    context.startActivity(intent)
                                },
                                onMarkSold = { vm.markAsSold(listing.id) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // ── FAB: Post listing ─────────────────────────────────────────────────
        ExtendedFloatingActionButton(
            onClick = { showPostSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Post Listing") },
            containerColor = MaterialTheme.colorScheme.primary
        )

        // ── Success banner ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.postSuccess,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2E7D32), shadowElevation = 8.dp) {
                Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Listing posted successfully!", color = Color.White,
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // ── Post Listing Bottom Sheet ─────────────────────────────────────────────
    if (showPostSheet) {
        PostListingSheet(
            vm = vm,
            onDismiss = { showPostSheet = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ListingCard(
    listing: FarmerListing,
    onContact: () -> Unit,
    onMarkSold: () -> Unit
) {
    val cropColour = cropColour(listing.crop)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header row ────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Crop avatar
                Box(modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(cropColour.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center) {
                    Text(listing.crop.take(1).uppercase(),
                        fontWeight = FontWeight.Bold, fontSize = 18.sp, color = cropColour)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(listing.crop, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(" ${listing.locationName}  ·  ${distanceLabel(listing.distanceKm)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                // Price badge
                Surface(shape = RoundedCornerShape(8.dp), color = cropColour.copy(alpha = 0.1f)) {
                    Text("₹${listing.pricePerKg.toInt()}/kg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = cropColour,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            // ── Details row ───────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailChip(Icons.Filled.Scale, "${listing.quantityKg.toInt()} kg available")
                DetailChip(Icons.Filled.Person, listing.farmerName)
            }

            Spacer(Modifier.height(14.dp))

            // ── Action buttons ────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onContact,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cropColour)
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Call Farmer", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = onMarkSold,
                    modifier = Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Mark Sold", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun DetailChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
            overflow = TextOverflow.Ellipsis)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostListingSheet(vm: MarketplaceViewModel, onDismiss: () -> Unit) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val crops = listOf("Soybean", "Wheat", "Tomato", "Potato", "Onion",
        "Chickpea", "Maize", "Mustard", "Garlic", "Rice", "Cotton", "Other")

    var selectedCrop    by remember { mutableStateOf(crops[0]) }
    var expandedCrop    by remember { mutableStateOf(false) }
    var quantity        by remember { mutableStateOf("") }
    var price           by remember { mutableStateOf("") }
    var locationName    by remember { mutableStateOf("") }
    var farmerName      by remember { mutableStateOf("") }
    var farmerPhone     by remember { mutableStateOf("") }

    // Dismiss on success
    LaunchedEffect(uiState.postSuccess) {
        if (uiState.postSuccess) onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Post New Listing", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Text("Your listing will be visible to buyers in real-time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider()

            // Crop dropdown
            Text("Crop *", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            ExposedDropdownMenuBox(expanded = expandedCrop,
                onExpandedChange = { expandedCrop = it }) {
                OutlinedTextField(
                    value = selectedCrop, onValueChange = {},
                    readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Filled.Eco, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCrop) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedCrop,
                    onDismissRequest = { expandedCrop = false }) {
                    crops.forEach { crop ->
                        DropdownMenuItem(text = { Text(crop) },
                            onClick = { selectedCrop = crop; expandedCrop = false })
                    }
                }
            }

            // Quantity + Price in a row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Quantity (kg) *", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = quantity, onValueChange = { quantity = it },
                        placeholder = { Text("e.g. 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Price/kg (₹) *", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = price, onValueChange = { price = it },
                        placeholder = { Text("e.g. 45") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Location name
            Text("Location *", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = locationName, onValueChange = { locationName = it },
                placeholder = { Text("e.g. Indore, Ujjain...") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            )

            // Farmer name
            Text("Your Name *", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = farmerName, onValueChange = { farmerName = it },
                placeholder = { Text("e.g. Ramesh Patidar") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            )

            // Phone
            Text("Phone Number *", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = farmerPhone, onValueChange = { farmerPhone = it },
                placeholder = { Text("10-digit mobile number") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            val isFormValid = selectedCrop.isNotBlank() && quantity.isNotBlank() &&
                price.isNotBlank() && locationName.isNotBlank() &&
                farmerName.isNotBlank() && farmerPhone.length >= 10

            Button(
                onClick = {
                    vm.postListing(
                        crop = selectedCrop,
                        quantityKg = quantity.toDoubleOrNull() ?: 0.0,
                        pricePerKg = price.toDoubleOrNull() ?: 0.0,
                        locationLat = 22.7196,  // default to Indore; farmer can update
                        locationLng = 75.8577,
                        locationName = locationName,
                        farmerName = farmerName,
                        farmerPhone = farmerPhone
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = isFormValid && !uiState.isPosting
            ) {
                if (uiState.isPosting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Posting...")
                } else {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Post Listing", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
