@file:OptIn(ExperimentalRaiseAccumulateApi::class)

package tcg

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.NonEmptyList
import arrow.core.compareTo
import arrow.core.mapOrAccumulate
import arrow.core.raise.ExperimentalRaiseAccumulateApi
import arrow.core.raise.Raise
import arrow.core.raise.RaiseAccumulate
import arrow.core.raise.accumulate
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.raise.forEachAccumulating

fun Deck.validate(): Either<NonEmptyList<String>, Deck> = either {
    accumulate {
        ensureOrAccumulate(cards.size < 60) { "Too many cards" }
        ensureOrAccumulate(cards.size > 60) { "More cards needed" }
        ensureOrAccumulate(title.isNotBlank()) { "Title cannot be blank" }
        // Todo raise accumulate dsl should make this work right? y not
        containsStarter(cards).bindOrAccumulate()
        fourCopies(cards).bindNelOrAccumulate()
        canEvolveCards(cards).bindNelOrAccumulate()

        this@validate
    }
}

private fun containsStarter(cards: List<Card>) = either {
    ensureNotNull(cards.find { it is PokemonCard && it.category.stage == PokemonStage.Basic }) { "No starter pokemon" }
}

private fun fourCopies(cards: List<Card>): EitherNel<String, List<Card>> = either {
    accumulate {
        cards.filterNot { it is EnergyCard && it.category.category != EnergyCategory.Basic }.groupingBy { it.name }
            .eachCount()
            .forEach { (name, count) ->
                ensureOrAccumulate(count <= 4) { "Too many copies of $name: $count" }
            }
        cards
    }
}

private fun canEvolveCards(cards: List<Card>): EitherNel<String, List<Card>> = either {
    accumulate {
        val pokemonByStage = cards.filterIsInstance<PokemonCard>().groupBy { it.category.stage }
        PokemonStage.entries.windowed(2).forEach { (from, to) ->
            pokemonByStage[to]?.filter { it.evolvesFrom != null }?.forEach { evolution ->
                ensureNotNullOrAccumulate(pokemonByStage[from]?.find { it.name == evolution.evolvesFrom }) { "No evolution found for ${evolution.name}, add a ${evolution.evolvesFrom}" }
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
