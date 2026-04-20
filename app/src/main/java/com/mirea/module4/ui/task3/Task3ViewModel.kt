package com.mirea.module4.ui.task3

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
data class GitHubRepo(
    val id: Int,
    val full_name: String,
    val description: String = "",
    val stargazers_count: Int = 0,
    val language: String = ""
)

sealed class SearchState {
    data object Idle : SearchState()
    data object Loading : SearchState()
    data class Success(val repos: List<GitHubRepo>) : SearchState()
    data class Error(val message: String) : SearchState()
}

class Task3ViewModel(app: Application) : AndroidViewModel(app) {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state: StateFlow<SearchState> = _state

    private var searchJob: Job? = null

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private var allRepos: List<GitHubRepo> = emptyList()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = app.assets.open("github_repos.json")
                val text = BufferedReader(InputStreamReader(stream)).readText()
                allRepos = json.decodeFromString<List<GitHubRepo>>(text)
            } catch (e: Exception) {
                allRepos = emptyList()
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _state.value = SearchState.Idle
            return
        }
        _state.value = SearchState.Loading
        searchJob = viewModelScope.launch {
            delay(500) // debounce 500 мс
            val results = withContext(Dispatchers.Default) {
                allRepos.filter { repo ->
                    repo.full_name.contains(newQuery, ignoreCase = true) ||
                    repo.description.contains(newQuery, ignoreCase = true) ||
                    repo.language.contains(newQuery, ignoreCase = true)
                }
            }
            _state.value = SearchState.Success(results)
        }
    }
}
