package com.mirea.module4.ui.task5

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mirea.module4.services.TimerForegroundService

// Задание 5: Счётчик времени с уведомлением (Foreground Service)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task5Screen(navController: NavController) {
    val context = LocalContext.current
    var seconds by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        else true
    )}

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    // Receive ticks from service
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                seconds = intent?.getIntExtra(TimerForegroundService.EXTRA_SECONDS, 0) ?: 0
            }
        }
        val filter = IntentFilter(TimerForegroundService.BROADCAST_TICK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        onDispose { context.unregisterReceiver(receiver) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание 5: Foreground Service") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isRunning) {
                            val stopIntent = Intent(context, TimerForegroundService::class.java)
                                .apply { action = TimerForegroundService.ACTION_STOP }
                            context.startService(stopIntent)
                        }
                        navController.popBackStack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Foreground Service: таймер продолжает работать даже при сворачивании приложения и отображается в шторке уведомлений.",
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

            Spacer(Modifier.height(16.dp))
            Text(
                text = formatTime(seconds),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isRunning) "Таймер запущен" else "Таймер остановлен",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        seconds = 0
                        isRunning = true
                        val intent = Intent(context, TimerForegroundService::class.java)
                            .apply { action = TimerForegroundService.ACTION_START }
                        context.startForegroundService(intent)
                    },
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f)
                ) { Text("Старт") }

                OutlinedButton(
                    onClick = {
                        isRunning = false
                        val intent = Intent(context, TimerForegroundService::class.java)
                            .apply { action = TimerForegroundService.ACTION_STOP }
                        context.startService(intent)
                    },
                    enabled = isRunning,
                    modifier = Modifier.weight(1f)
                ) { Text("Стоп") }
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
