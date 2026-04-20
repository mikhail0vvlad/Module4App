package com.mirea.module4.ui.task6

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mirea.module4.services.TimerBackgroundService

// Задание 6: Одноразовый таймер с уведомлением (Background Service)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task6Screen(navController: NavController) {
    val context = LocalContext.current
    var durationText by remember { mutableStateOf("30") }
    var isStarted by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        else true
    )}

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание 6: Background Service") },
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
                    "Background Service: сервис ожидает указанное время, затем показывает уведомление и сам себя останавливает.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Разрешить уведомления") }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = durationText,
                onValueChange = {
                    durationText = it.filter { c -> c.isDigit() }.take(4)
                    isStarted = false
                },
                label = { Text("Время (секунды)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val duration = durationText.toIntOrNull()?.coerceIn(1, 3600) ?: 30
                    durationText = duration.toString()
                    isStarted = true
                    val intent = Intent(context, TimerBackgroundService::class.java)
                        .putExtra(TimerBackgroundService.EXTRA_DURATION, duration)
                    context.startService(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isStarted
            ) {
                Text("Запустить таймер")
            }

            if (isStarted) {
                val duration = durationText.toIntOrNull() ?: 30
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Ждём ${duration} сек... Уведомление появится в шторке")
                    }
                }
                OutlinedButton(
                    onClick = { isStarted = false },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Новый таймер") }
            }
        }
    }
}
