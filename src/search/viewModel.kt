package search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tcg.Card
import tcg.api.KtorPokemonTcgApi
import tcg.api.PokemonRepo
import tcg.api.ResilientPokemonApi
import kotlin.time.Duration.Companion.milliseconds

sealed interface SearchStatus {
    data class Loading(val job: Job) : SearchStatus
    data class Ok(val results: List<Card>) : SearchStatus {
        val isEmpty: Boolean = results.isEmpty()
    }

    data class Error(val message: String?) : SearchStatus
}

class SearchViewModel(
    private val repo: PokemonRepo = PokemonRepo(ResilientPokemonApi(KtorPokemonTcgApi()))
) : ViewModel() {
    private val _options = mutableStateOf(SearchOptions.INITIAL)
    val options: SearchOptions by _options

    private val _result = mutableStateOf<SearchStatus>(SearchStatus.Ok(emptyList()))
    val result: SearchStatus by _result

    fun updateText(newText: String) {
        _options.value = _options.value.copy(text = newText)

        // cancel previous job if loading
        (_result.value as? SearchStatus.Loading)?.job?.cancel()
        // now start the new job
        _result.value = SearchStatus.Loading(
            viewModelScope.launch {
                // give time for the previous job to cancel
                delay(500.milliseconds)
                repo.search(newText)
                    .fold(
                        ifLeft = { _result.value = SearchStatus.Error(it.localizedMessage) },
                        ifRight = { _result.value = SearchStatus.Ok(it) }
                    )
            }
        )
    }
}

data class SearchOptions(val text: String) {
    companion object {
        val INITIAL: SearchOptions = SearchOptions("")
    }
}