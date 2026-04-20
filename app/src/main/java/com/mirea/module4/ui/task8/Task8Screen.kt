package com.mirea.module4.ui.task8

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
import androidx.navigation.NavController
import androidx.work.*
import com.mirea.module4.workers.*
import kotlinx.coroutines.launch

// Задание 8: WorkManager — последовательная цепочка обработки фото

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task8Screen(navController: NavController) {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    var statusText by remember { mutableStateOf("Готов к запуску") }
    var progress by remember { mutableFloatStateOf(0f) }
    var isWorking by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание 8: WorkManager (цепочка)") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "WorkManager: последовательная цепочка\n" +
                    "Worker 1 → Сжатие фото\n" +
                    "Worker 2 → Добавление водяного знака\n" +
                    "Worker 3 → Загрузка в облако",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            // Status
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (isWorking) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            result?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(it, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    result = null
                    isWorking = true
                    progress = 0f
                    val filename = "photo_${System.currentTimeMillis()}.jpg"
                    val inputData = workDataOf(KEY_FILENAME to filename)

                    val compress = OneTimeWorkRequestBuilder<CompressPhotoWorker>()
                        .setInputData(inputData)
                        .build()
                    val watermark = OneTimeWorkRequestBuilder<WatermarkWorker>().build()
                    val upload = OneTimeWorkRequestBuilder<UploadWorker>().build()

                    workManager.beginUniqueWork(
                        "photo_chain",
                        ExistingWorkPolicy.REPLACE,
                        compress
                    ).then(watermark).then(upload).enqueue()

                    scope.launch {
                        // Observe compress
                        workManager.getWorkInfoByIdFlow(compress.id).collect { info ->
                            if (info?.state == WorkInfo.State.RUNNING) {
                                val p = info.progress.getInt(KEY_PROGRESS, 0)
                                val s = info.progress.getString(KEY_STATUS) ?: "Сжимаем..."
                                progress = p / 300f // 3 workers
                                statusText = s
                            }
                        }
                    }
                    scope.launch {
                        workManager.getWorkInfoByIdFlow(watermark.id).collect { info ->
                            if (info?.state == WorkInfo.State.RUNNING) {
                                val p = info.progress.getInt(KEY_PROGRESS, 0)
                                val s = info.progress.getString(KEY_STATUS) ?: "Водяной знак..."
                                progress = (100 + p) / 300f
                                statusText = s
                            }
                        }
                    }
                    scope.launch {
                        workManager.getWorkInfoByIdFlow(upload.id).collect { info ->
                            if (info?.state == WorkInfo.State.RUNNING) {
                                val p = info.progress.getInt(KEY_PROGRESS, 0)
                                val s = info.progress.getString(KEY_STATUS) ?: "Загружаем..."
                                progress = (200 + p) / 300f
                                statusText = s
                            } else if (info?.state == WorkInfo.State.SUCCEEDED) {
                                val r = info.outputData.getString(KEY_RESULT) ?: "Готово"
                                val f = info.outputData.getString(KEY_FILENAME) ?: filename
                                result = "$r\nФайл: $f"
                                statusText = "Завершено!"
                                progress = 1f
                                isWorking = false
                            } else if (info?.state == WorkInfo.State.FAILED) {
                                statusText = "Ошибка!"
                                isWorking = false
                            }
                        }
                    }
                },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isWorking) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Начать обработку и загрузку")
            }
        }
    }
}
