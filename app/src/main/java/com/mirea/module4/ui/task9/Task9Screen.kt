package com.mirea.module4.ui.task9

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// Задание 9: WorkManager — параллельная загрузка погоды для городов

data class CityWeather(val city: String, val temp: Int?, val state: WorkInfo.State)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task9Screen(navController: NavController) {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val cities = listOf("Москва", "Лондон", "Нью-Йорк", "Токио")
    var cityStates by remember { mutableStateOf(cities.map { CityWeather(it, null, WorkInfo.State.BLOCKED) }) }
    var isWorking by remember { mutableStateOf(false) }
    var reportText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание 9: WorkManager (параллельно)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "WorkManager: параллельные Workers для каждого города\n→ Финальный Worker объединяет и формирует отчёт\nУведомление обновляется по ходу выполнения.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cityStates) { city ->
                    CityCard(city)
                }
            }

            reportText?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(it, modifier = Modifier.padding(12.dp))
                }
            }

            Button(
                onClick = {
                    reportText = null
                    isWorking = true
                    cityStates = cities.map { CityWeather(it, null, WorkInfo.State.ENQUEUED) }

                    val weatherRequests = cities.map { city ->
                        OneTimeWorkRequestBuilder<WeatherWorker>()
                            .setInputData(workDataOf(KEY_CITY to city))
                            .addTag("weather_$city")
                            .build()
                    }
                    val reportRequest = OneTimeWorkRequestBuilder<WeatherReportWorker>()
                        .setInputMerger(ArrayCreatingInputMerger::class)
                        .build()

                    WorkManager.getInstance(context)
                        .beginUniqueWork("weather_parallel", ExistingWorkPolicy.REPLACE,
                            weatherRequests.first())
                        .also { chain ->
                            weatherRequests.drop(1).forEach { /* parallel via enqueue */ }
                        }

                    workManager.enqueue(weatherRequests)
                    workManager.beginWith(weatherRequests).then(reportRequest).enqueue()

                    weatherRequests.forEachIndexed { idx, req ->
                        scope.launch {
                            workManager.getWorkInfoByIdFlow(req.id).collect { info ->
                                if (info != null) {
                                    val temp = if (info.state == WorkInfo.State.SUCCEEDED)
                                        info.outputData.getInt(KEY_TEMP, 0) else null
                                    val updated = cityStates.toMutableList()
                                    if (idx < updated.size) {
                                        updated[idx] = updated[idx].copy(state = info.state, temp = temp)
                                        cityStates = updated.toList()
                                    }
                                }
                            }
                        }
                    }
                    scope.launch {
                        workManager.getWorkInfoByIdFlow(reportRequest.id).collect { info ->
                            if (info?.state == WorkInfo.State.SUCCEEDED) {
                                reportText = info.outputData.getString(KEY_REPORT) ?: "Отчёт готов!"
                                isWorking = false
                            } else if (info?.state == WorkInfo.State.FAILED) {
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
                Text("Собрать прогноз")
            }
        }
    }
}

@Composable
fun CityCard(city: CityWeather) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🌍", modifier = Modifier.padding(end = 8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(city.city, fontWeight = FontWeight.Bold)
                Text(
                    when (city.state) {
                        WorkInfo.State.ENQUEUED -> "В очереди..."
                        WorkInfo.State.RUNNING -> "Загружаем..."
                        WorkInfo.State.SUCCEEDED -> "Готово"
                        WorkInfo.State.FAILED -> "Ошибка"
                        WorkInfo.State.BLOCKED -> "Ожидание"
                        WorkInfo.State.CANCELLED -> "Отменено"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (city.state) {
                        WorkInfo.State.SUCCEEDED -> MaterialTheme.colorScheme.primary
                        WorkInfo.State.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            when (city.state) {
                WorkInfo.State.RUNNING -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                WorkInfo.State.SUCCEEDED -> {
                    city.temp?.let {
                        Text(
                            "${if (it > 0) "+" else ""}${it}°C",
                            fontWeight = FontWeight.Bold,
                            color = if (it > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                WorkInfo.State.FAILED -> Text("⚠️")
                else -> {}
            }
        }
    }
}
