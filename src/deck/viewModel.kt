package deck

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import arrow.core.NonEmptyList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import tcg.Card
import tcg.Deck
import tcg.DeckError
import tcg.validate
import utils.map

sealed interface DeckOperation {
    data class ChangeTitle(val newTitle: String) : DeckOperation
    data class AddCard(val card: Card) : DeckOperation
    data object Clear : DeckOperation
}

class DeckViewModel : ViewModel() {
    private val actions = MutableStateFlow<List<DeckOperation>>(emptyList())
    private val _deck = mutableStateOf(Deck.INITIAL)
    val deck: Deck by _deck

    val problems: NonEmptyList<DeckError>? by _deck.map { it.validate().leftOrNull() }

    private fun changeTitle(newTitle: String) {
        _deck.update { it.copy(title = newTitle) }
    }

    private fun clear() {
        _deck.update { it.copy(cards = emptyList()) }
    }

    private fun add(card: Card) {
        _deck.update { it.copy(cards = it.cards + card) }
    }

    fun apply(operation: DeckOperation) {
        when (operation) {
            is DeckOperation.ChangeTitle -> {
                changeTitle(operation.newTitle)
            }

            is DeckOperation.AddCard -> {
                add(card = operation.card)
            }

            DeckOperation.Clear -> {
                clear()
            }
        }
        actions.update { it + operation }
    }

    fun undo() {
        clear()
        val redo = actions.value.dropLast(1)
        actions.update { emptyList() }
        redo.forEach {
            apply(it)
        }
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
