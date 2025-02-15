package deck

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.NonEmptyList
import arrow.fx.coroutines.parMapNotNull
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tcg.Card
import tcg.Deck
import tcg.DeckError
import tcg.api.KtorPokemonTcgApi
import tcg.api.PokemonRepo
import tcg.api.ResilientPokemonApi
import tcg.validate
import utils.map
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed interface DeckOperation {
    sealed interface EditDeck : DeckOperation
    data class ChangeTitle(val newTitle: String) : EditDeck
    data class AddCard(val card: Card) : EditDeck
    data class RemoveCard(val card: Card) : EditDeck
    data object Clear : EditDeck
    data object Undo : DeckOperation
    data object Redo : DeckOperation
}

@OptIn(ExperimentalContracts::class)
inline fun guard(expression: Boolean, elseBlock: () -> Nothing) {
    contract {
        callsInPlace(elseBlock, InvocationKind.AT_MOST_ONCE)
        returns() implies (expression)
    }
    if (!expression) elseBlock()
}

data class DeckState(
    val past: List<Deck> = emptyList(),
    val deck: Deck,
    val future: List<Deck> = emptyList(),
) {
    init {
        println(this)
    }

    val hasRedo = future.isNotEmpty()
    val hasUndo = past.isNotEmpty()
}

class DeckViewModel(private val repo: PokemonRepo = PokemonRepo(ResilientPokemonApi(KtorPokemonTcgApi()))) :
    ViewModel() {
    private val _deck = mutableStateOf(DeckState(deck = Deck.INITIAL))
    val deckState: DeckState by _deck

    val problems: NonEmptyList<DeckError>? by _deck.map { it.deck.validate().leftOrNull() }

    fun apply(operation: DeckOperation) {
        _deck.update {
            reduceDeckState(it, operation)
        }
    }


    fun saveDeck(file: PlatformFile?) {
        guard(file != null) { return }
        viewModelScope.launch(Dispatchers.IO) {
            val text = deckState.deck.cards.joinToString("\n") { it.identifier }
            file.file.writeText(deckState.deck.title + "\n$text")
        }
    }

    fun loadFile(file: PlatformFile?) {
        guard(file != null) { return }

        viewModelScope.launch(Dispatchers.IO) {
            val lines = file.file.readLines()
            val title = lines.firstOrNull() ?: error("empty file")
            val cards = lines.drop(1).parMapNotNull { line ->
                line.takeIf { it.isNotBlank() }?.let { repo.getById(it) }
            }
            _deck.update { DeckState(deck = Deck(cards = cards, title = title)) }
        }
    }

    private fun reduceDeckState(deckState: DeckState, operation: DeckOperation): DeckState = when (operation) {
        is DeckOperation.EditDeck -> DeckState(
            deckState.past + deckState.deck,
            reduceDeck(deckState.deck, operation),
            emptyList()
        )

        DeckOperation.Undo -> {
            deckState.past.lastOrNull()?.let { new ->
                deckState.copy(
                    past = deckState.past.dropLast(1),
                    deck = new,
                    future = deckState.future + deckState.deck,
                )
            } ?: deckState
        }

        DeckOperation.Redo -> {
            deckState.future.lastOrNull()?.let { new ->
                deckState.copy(
                    past = deckState.past + deckState.deck,
                    deck = new,
                    future = deckState.future.dropLast(1),
                )
            } ?: deckState
        }
    }

    private fun reduceDeck(state: Deck, operation: DeckOperation.EditDeck): Deck = when (operation) {
        is DeckOperation.ChangeTitle -> {
            state.copy(title = operation.newTitle)
        }

        is DeckOperation.AddCard -> {
            state.copy(cards = state.cards + operation.card)
        }

        DeckOperation.Clear -> {
            state.copy(cards = emptyList())
        }

        is DeckOperation.RemoveCard -> state.copy(cards = state.cards - operation.card)
    }

    fun reset() {
        _deck.update { DeckState(deck = Deck.INITIAL) }
    }
}


// FROM ARROW-OPTICS-COMPOSE
/**
 * Modifies the value in this [MutableState]
 * by applying the function [block] to the current value.
 */
inline fun <T> MutableState<T>.update(crossinline block: (T) -> T) {
    Snapshot.withMutableSnapshot {
        value = block(value)
    }
}
