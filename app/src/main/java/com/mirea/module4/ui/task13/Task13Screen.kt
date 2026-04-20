package com.mirea.module4.ui.task13

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

// Задание 13: StateFlow — живой курс валют

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task13Screen(navController: NavController, vm: Task13ViewModel = viewModel()) {
    val rate by vm.rate.collectAsState()
    val direction by vm.direction.collectAsState()
    val lastUpdate by vm.lastUpdate.collectAsState()

    val rateColor by animateColorAsState(
        targetValue = when (direction) {
            1 -> Color(0xFF2E7D32)   // зелёный — рост
            -1 -> Color(0xFFD32F2F)  // красный — падение
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(500),
        label = "rateColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание 13: StateFlow") },
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
                    "StateFlow: горячий поток с последним значением.\nКурс автоматически обновляется каждые 5 сек.\nПри повороте экрана значение сохраняется (ViewModel).",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "USD / RUB",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (direction) {
                        1 -> Icons.Default.ArrowUpward
                        -1 -> Icons.Default.ArrowDownward
                        else -> Icons.Default.Remove
                    },
                    contentDescription = null,
                    tint = rateColor,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "%.2f ₽".format(rate),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = rateColor
                )
            }

            if (lastUpdate.isNotEmpty()) {
                Text(
                    "Обновлено: $lastUpdate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("История изменений:", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Автообновление каждые 5 секунд\nСтрелка ▲ — курс вырос (зелёный)\nСтрелка ▼ — курс упал (красный)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { vm.forceUpdate() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Обновить сейчас", fontSize = 16.sp)
            }
        }
    }
}
