package deck

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import arrow.core.NonEmptyList
import tcg.Card
import tcg.Deck
import tcg.DeckError
import tcg.validate
import utils.map

sealed interface DeckOperation {
    sealed interface EditDeck : DeckOperation
    data class ChangeTitle(val newTitle: String) : EditDeck
    data class AddCard(val card: Card) : EditDeck
    data class RemoveCard(val card: Card) : EditDeck
    data object Clear : EditDeck
    data object Undo : DeckOperation
    data object Redo : DeckOperation
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

class DeckViewModel : ViewModel() {
    private val _deck = mutableStateOf(DeckState(deck = Deck.INITIAL))
    val deckState: DeckState by _deck

    val problems: NonEmptyList<DeckError>? by _deck.map { it.deck.validate().leftOrNull() }

    fun apply(operation: DeckOperation) {
        _deck.update {
            reduceDeckState(it, operation)
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
public inline fun <T> MutableState<T>.update(crossinline block: (T) -> T) {
    Snapshot.withMutableSnapshot {
        value = block(value)
    }
}
