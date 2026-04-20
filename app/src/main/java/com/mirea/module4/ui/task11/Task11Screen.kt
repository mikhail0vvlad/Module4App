package com.mirea.module4.ui.task11

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mirea.module4.receivers.BootReceiver
import java.text.SimpleDateFormat
import java.util.*

// Задание 11: AlarmManager — ежедневное напоминание о таблетке

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task11Screen(navController: NavController) {
    val context = LocalContext.current
    val prefs = BootReceiver.getPrefs(context)

    var isEnabled by remember { mutableStateOf(prefs.getBoolean("alarm_enabled", false)) }
    var nextAlarmTime by remember { mutableLongStateOf(prefs.getLong("next_alarm", 0L)) }
    var hasNotifPermission by remember { mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        else true
    )}

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotifPermission = granted }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        alarmManager.canScheduleExactAlarms() else true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание 11: AlarmManager") },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Напоминание о таблетке",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (isEnabled && nextAlarmTime > 0) {
                val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                Text(
                    "Следующее напоминание:\n${sdf.format(Date(nextAlarmTime))}",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            // Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isEnabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                )
                Text(
                    text = if (isEnabled) "Включено" else "Выключено",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = if (isEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            // Permission warnings
            if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                OutlinedButton(
                    onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Разрешить уведомления") }
            }
            if (!canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:${context.packageName}")))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Разрешить точные будильники") }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (!isEnabled) {
                        BootReceiver.scheduleAlarm(context)
                        isEnabled = true
                        nextAlarmTime = prefs.getLong("next_alarm", 0L)
                    } else {
                        BootReceiver.cancelAlarm(context)
                        isEnabled = false
                        nextAlarmTime = 0L
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = if (isEnabled)
                    ButtonDefaults.outlinedButtonColors()
                else
                    ButtonDefaults.buttonColors(),
                border = if (isEnabled) ButtonDefaults.outlinedButtonBorder else null
            ) {
                Text(
                    if (isEnabled) "Выключить напоминание" else "Включить напоминание",
                    fontSize = 16.sp
                )
            }
        }
    }
}
