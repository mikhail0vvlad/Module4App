package com.mirea.module4.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mirea.module4.navigation.Screen

data class TaskItem(
    val screen: Screen,
    val number: Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val group: String
)

val taskList = listOf(

    TaskItem(Screen.Task3, 3, "Поиск репозиториев GitHub", "Корутины: debounce, отмена задач, LazyColumn", Icons.Default.Search, "Корутины"),
    TaskItem(Screen.Task4, 4, "Социальная лента", "Корутины: async/await, supervisorScope, параллельная загрузка", Icons.Default.Feed, "Корутины"),
    TaskItem(Screen.Task5, 5, "Счётчик времени", "Foreground Service: таймер в уведомлении", Icons.Default.Timer, "Сервисы"),
    TaskItem(Screen.Task6, 6, "Одноразовый таймер", "Background Service: уведомление по истечении времени", Icons.Default.Alarm, "Сервисы"),
    TaskItem(Screen.Task7, 7, "Случайное число", "Bound Service: генерация числа каждую секунду", Icons.Default.Casino, "Сервисы"),
    TaskItem(Screen.Task8, 8, "Обработка фото (цепочка)", "WorkManager: последовательная цепочка Workers", Icons.Default.PhotoCamera, "WorkManager"),
    TaskItem(Screen.Task9, 9, "Прогноз погоды", "WorkManager: параллельная обработка + уведомление", Icons.Default.WbSunny, "WorkManager"),
    TaskItem(Screen.Task10, 10, "Геолокация и адрес", "FusedLocationProvider + обратное геокодирование", Icons.Default.LocationOn, "Локация"),
    TaskItem(Screen.Task11, 11, "Напоминание о таблетке", "AlarmManager: ежедневное уведомление в 20:00", Icons.Default.Medication, "Уведомления"),
    TaskItem(Screen.Task12, 12, "Факты о животных", "Cold Flow: генератор случайных фактов", Icons.Default.Pets, "Flow"),
    TaskItem(Screen.Task13, 13, "Курс валют", "StateFlow: живое обновление курса каждые 5 сек", Icons.Default.CurrencyRuble, "Flow"),
    TaskItem(Screen.Task14, 14, "Компас", "Сенсоры: TYPE_ACCELEROMETER + TYPE_MAGNETIC_FIELD", Icons.Default.Explore, "Сенсоры"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Модуль 4", fontWeight = FontWeight.Bold)
                        Text("Android Kotlin", style = MaterialTheme.typography.labelSmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val groups = taskList.groupBy { it.group }
            groups.forEach { (group, tasks) ->
                item {
                    Text(
                        text = group,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(tasks) { task ->
                    TaskCard(task = task, onClick = { navController.navigate(task.screen.route) })
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: TaskItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Icon(
                        imageVector = task.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Задание ${task.number}: ${task.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
