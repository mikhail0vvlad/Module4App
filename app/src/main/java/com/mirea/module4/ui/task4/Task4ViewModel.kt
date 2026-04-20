package com.mirea.module4.ui.task4

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

@Serializable
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
    val avatarUrl: String = ""
)

@Serializable
data class Comment(
    val postId: Int,
    val id: Int,
    val name: String,
    val body: String
)

sealed class PostLoadState {
    data object Loading : PostLoadState()
    data class Ready(val avatarColor: String, val comments: List<Comment>) : PostLoadState()
    data object Error : PostLoadState()
}

data class PostUiItem(
    val post: Post,
    val state: PostLoadState = PostLoadState.Loading
)

class Task4ViewModel(app: Application) : AndroidViewModel(app) {

    private val _posts = MutableStateFlow<List<PostUiItem>>(emptyList())
    val posts: StateFlow<List<PostUiItem>> = _posts

    private var loadingJob: Job? = null

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val avatarColors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
        "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9")

    init { load() }

    fun refresh() {
        loadingJob?.cancel()
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val postsData = withContext(Dispatchers.IO) {
                try {
                    val stream = getApplication<Application>().assets.open("social_posts.json")
                    json.decodeFromString<List<Post>>(BufferedReader(InputStreamReader(stream)).readText())
                } catch (e: Exception) { emptyList() }
            }
            val commentsData = withContext(Dispatchers.IO) {
                try {
                    val stream = getApplication<Application>().assets.open("comments.json")
                    json.decodeFromString<List<Comment>>(BufferedReader(InputStreamReader(stream)).readText())
                } catch (e: Exception) { emptyList() }
            }

            _posts.value = postsData.map { PostUiItem(it, PostLoadState.Loading) }

            supervisorScope {
                postsData.forEachIndexed { idx, post ->
                    launch {
                        val result = try {
                            val avatarDeferred = async {
                                delay((500..1500).random().toLong())
                                avatarColors[post.userId % avatarColors.size]
                            }
                            val commentsDeferred = async {
                                delay((300..1200).random().toLong())
                                commentsData.filter { it.postId == post.id }
                            }
                            PostLoadState.Ready(avatarDeferred.await(), commentsDeferred.await())
                        } catch (e: Exception) {
                            PostLoadState.Error
                        }
                        val current = _posts.value.toMutableList()
                        if (idx < current.size) {
                            current[idx] = current[idx].copy(state = result)
                            _posts.value = current.toList()
                        }
                    }
                }
            }
        }
    }
}
