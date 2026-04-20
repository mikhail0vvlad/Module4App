package com.mirea.module4.ui.task12

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// Задание 12: Cold Flow — генератор случайных фактов о животных

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task12Screen(navController: NavController, vm: Task12ViewModel = viewModel()) {
    var currentFact by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание 12: Cold Flow") },
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
                    "Cold Flow: каждый нажатие кнопки запускает новый collect().\nПредыдущий факт исчезает, загружается новый с задержкой 1.5–3 сек.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Crossfade вместо AnimatedVisibility — без привязки к ColumnScope
                Crossfade(
                    targetState = Triple(isLoading, currentFact, isVisible),
                    animationSpec = tween(400),
                    label = "factState"
                ) { (loading, fact, visible) ->
                    when {
                        loading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(12.dp))
                                Text("Загружаем факт...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        visible && fact != null -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text(
                                    text = fact,
                                    modifier = Modifier.padding(24.dp),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 30.sp
                                )
                            }
                        }
                        else -> {
                            Text(
                                "Нажмите кнопку, чтобы узнать случайный факт о животном",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    isLoading = true
                    isVisible = false
                    scope.launch {
                        // Каждый collect — новый запуск cold flow
                        vm.getRandomFact().collect { fact ->
                            currentFact = fact
                            isLoading = false
                            isVisible = true
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Новый факт! 🐾", fontSize = 16.sp)
            }
        }
    }
}
