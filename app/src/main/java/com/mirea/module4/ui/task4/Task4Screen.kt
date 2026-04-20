package com.mirea.module4.ui.task4

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

// Задание 4: Социальная лента с постами, аватарками и комментариями

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task4Screen(navController: NavController, vm: Task4ViewModel = viewModel()) {
    val posts by vm.posts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание 4: Социальная лента") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        if (posts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts) { item -> PostCard(item) }
            }
        }
    }
}

@Composable
fun PostCard(item: PostUiItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                when (val s = item.state) {
                    is PostLoadState.Loading -> {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                    is PostLoadState.Ready -> {
                        val color = try { Color(android.graphics.Color.parseColor(s.avatarColor)) }
                        catch (e: Exception) { MaterialTheme.colorScheme.secondary }
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                item.post.title.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    is PostLoadState.Error -> {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("?", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(item.post.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Пользователь #${item.post.userId}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(item.post.body, style = MaterialTheme.typography.bodyMedium)

            // Comments section
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(4.dp))
            when (val s = item.state) {
                is PostLoadState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("Загрузка комментариев...", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                is PostLoadState.Ready -> {
                    if (s.comments.isEmpty()) {
                        Text("Нет комментариев", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("💬 ${s.comments.size} комментари${if (s.comments.size == 1) "й" else "ев"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                        s.comments.take(2).forEach { comment ->
                            Text(
                                "• ${comment.name}: ${comment.body}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                        if (s.comments.size > 2) {
                            Text("...ещё ${s.comments.size - 2}", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                is PostLoadState.Error -> {
                    Text("⚠️ Ошибка загрузки", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
