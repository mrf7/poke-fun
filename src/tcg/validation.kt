@file:OptIn(ExperimentalRaiseAccumulateApi::class)

package tcg

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.Nel
import arrow.core.NonEmptyList
import arrow.core.compareTo
import arrow.core.mapValuesNotNull
import arrow.core.raise.ExperimentalRaiseAccumulateApi
import arrow.core.raise.accumulate
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull

sealed interface DeckError {
    val message: String

    data class DeckTooLarge(val size: Int, val expected: Int) : DeckError {
        override val message: String = "Deck too large: $size, should be $expected"
    }

    data class DeckTooSmall(val size: Int, val expected: Int) : DeckError {
        override val message: String = "Deck too small: $size, should be $expected"
    }

    data object EmptyName : DeckError {
        override val message: String = "Title cannot be blank"
    }

    data object NoStarter : DeckError {
        override val message: String = "Deck must have a starter"
    }

    data class TooManyCopies(val cards: NonEmptyList<Card>, val expected: Int) : DeckError {
        override val message: String =
            "Too many copies of ${cards.first().name},expected: $expected, actual: ${cards.size}"
    }

    data class CannotEvolve(val card: PokemonCard) : DeckError {
        override val message: String = "No evolution found for ${card.name}, add a ${card.evolvesFrom}"
    }
}

fun Deck.validate(): Either<NonEmptyList<DeckError>, Deck> = either {
    accumulate {
        ensureOrAccumulate(cards.size < 60) { DeckError.DeckTooLarge(cards.size, 60) }
        ensureOrAccumulate(cards.size > 60) { DeckError.DeckTooSmall(cards.size, 60) }
        ensureOrAccumulate(title.isNotBlank()) { DeckError.EmptyName }
        // Todo raise accumulate dsl should make this work right? y not
        containsStarter(cards).bindOrAccumulate()
        fourCopies(cards).bindNelOrAccumulate()
        canEvolveCards(cards).bindNelOrAccumulate()

        this@validate
    }
}

private fun containsStarter(cards: List<Card>) = either {
    ensureNotNull(cards.find { it is PokemonCard && it.category.stage == PokemonStage.Basic }) { DeckError.NoStarter }
}

private fun fourCopies(cards: List<Card>): EitherNel<DeckError, List<Card>> = either {
    accumulate {
        cards.filterNot { it is EnergyCard && it.category.category != EnergyCategory.Basic }
            .groupBy({ it.name })
            .mapValuesNotNull { (_, cards) -> cards.toNonEmptyListOrNull() }
            .forEach { (_, cards) ->
                ensureOrAccumulate(cards.size <= 4) { DeckError.TooManyCopies(cards, 4) }
            }
        cards
    }
}

private fun canEvolveCards(cards: List<Card>): EitherNel<DeckError, List<Card>> = either {
    accumulate {
        val pokemonByStage = cards.filterIsInstance<PokemonCard>().groupBy { it.category.stage }
        PokemonStage.entries.windowed(2).forEach { (from, to) ->
            pokemonByStage[to]?.filter { it.evolvesFrom != null }?.forEach { evolution ->
                ensureNotNullOrAccumulate(pokemonByStage[from]?.find { it.name == evolution.evolvesFrom }) {
                    DeckError.CannotEvolve(
                        evolution
                    )
                }
            }
        }
        cards
    }
}
//private fun RaiseAccumulate<String>.fourCopies(cards: List<Card>) = either {
//    val groups =
//        cards.filterNot { it is EnergyCard && it.category.category != EnergyCategory.Basic }.groupingBy { it.name }
//            .eachCount()
//    (groups).mapOrAccumulate { (name, count) ->
//
//        ensure(count < 4) { "Too many copies of $name: $count" }
//    }
//}
