package com.mirea.module4.ui.task10

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Задание 10: Геолокация и обратное геокодирование

sealed class LocationState {
    data object Idle : LocationState()
    data object Loading : LocationState()
    data class Success(val address: String, val lat: Double, val lng: Double) : LocationState()
    data class Error(val message: String) : LocationState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task10Screen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var locationState by remember { mutableStateOf<LocationState>(LocationState.Idle) }
    var hasPermission by remember { mutableStateOf(
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    )}

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание 10: Геолокация") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "FusedLocationProviderClient + Geocoder:\nОпределение координат и преобразование в читаемый адрес.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = when (locationState) {
                    is LocationState.Success -> MaterialTheme.colorScheme.primary
                    is LocationState.Error -> MaterialTheme.colorScheme.error
                    is LocationState.Loading -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            when (val s = locationState) {
                is LocationState.Idle -> {
                    Text("Нажмите кнопку для определения адреса",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is LocationState.Loading -> {
                    CircularProgressIndicator()
                    Text("Определяем местоположение...")
                }
                is LocationState.Success -> {
                    Text(
                        s.address,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Широта: ${"%.6f".format(s.lat)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Долгота: ${"%.6f".format(s.lng)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is LocationState.Error -> {
                    Text(
                        s.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (!hasPermission) {
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                        return@Button
                    }
                    locationState = LocationState.Loading
                    scope.launch {
                        locationState = getAddressFromLocation(context)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = locationState !is LocationState.Loading
            ) {
                Text(if (!hasPermission) "Запросить разрешение" else "Получить мой адрес")
            }
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun getAddressFromLocation(context: Context): LocationState {
    return try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        val location = suspendCancellableCoroutine<Location?> { cont ->
            cont.invokeOnCancellation { cts.cancel() }
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc -> cont.resume(loc) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        } ?: return LocationState.Error("GPS недоступен. Убедитесь, что GPS включён.")

        val address = getAddressFromCoordinates(context, location.latitude, location.longitude)
        LocationState.Success(address, location.latitude, location.longitude)
    } catch (e: Exception) {
        LocationState.Error("Ошибка: ${e.localizedMessage ?: "Неизвестная ошибка"}")
    }
}

@Suppress("DEPRECATION")
private suspend fun getAddressFromCoordinates(context: Context, lat: Double, lng: Double): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { cont ->
            geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    val addr = addresses.firstOrNull()
                    cont.resume(addr?.let { formatAddress(it) } ?: "Адрес не определён (${lat}, ${lng})")
                }
                override fun onError(errorMessage: String?) {
                    cont.resume("Ошибка геокодирования: $errorMessage")
                }
            })
        }
    } else {
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        val addr = addresses?.firstOrNull()
        addr?.let { formatAddress(it) } ?: "Адрес не определён (${lat}, ${lng})"
    }
}

private fun formatAddress(address: Address): String {
    val parts = mutableListOf<String>()
    address.thoroughfare?.let { parts.add(it) }
    address.subThoroughfare?.let { if (parts.isNotEmpty()) parts[0] = "${parts[0]}, $it" }
    address.locality?.let { parts.add(it) }
    address.adminArea?.let { if (it != address.locality) parts.add(it) }
    address.countryName?.let { parts.add(it) }
    return parts.joinToString("\n")
}
