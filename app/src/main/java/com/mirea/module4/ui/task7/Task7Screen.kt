package com.mirea.module4.ui.task7

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import com.mirea.module4.services.RandomNumberService

// Задание 7: Bound Service — случайное число каждую секунду

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task7Screen(navController: NavController) {
    val context = LocalContext.current
    var service by remember { mutableStateOf<RandomNumberService?>(null) }
    var isBound by remember { mutableStateOf(false) }
    val number by (service?.number ?: kotlinx.coroutines.flow.MutableStateFlow(0)).collectAsState()

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = (binder as RandomNumberService.RandomBinder).getService()
                isBound = true
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
                isBound = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isBound) context.unbindService(connection)
        }
    }

    val bgColor by animateColorAsState(
        targetValue = if (isBound) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(500),
        label = "bg"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание 7: Bound Service") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isBound) context.unbindService(connection)
                        navController.popBackStack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Bound Service: клиент привязывается к сервису через ServiceConnection и получает прямой доступ к объекту сервиса.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = bgColor),
                modifier = Modifier.size(200.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isBound) {
                            Text(
                                text = number.toString(),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text("0..100", style = MaterialTheme.typography.labelSmall)
                        } else {
                            Text("—", fontSize = 48.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Не подключён", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        val intent = Intent(context, RandomNumberService::class.java)
                        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                    },
                    enabled = !isBound,
                    modifier = Modifier.weight(1f)
                ) { Text("Подключиться") }

                OutlinedButton(
                    onClick = {
                        context.unbindService(connection)
                        service = null
                        isBound = false
                    },
                    enabled = isBound,
                    modifier = Modifier.weight(1f)
                ) { Text("Отключиться") }
            }
        }
    }
}
