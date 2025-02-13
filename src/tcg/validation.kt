package tcg

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.ExperimentalRaiseAccumulateApi
import arrow.core.raise.accumulate
import arrow.core.raise.either

@OptIn(ExperimentalRaiseAccumulateApi::class)
fun Deck.validate(): Either<NonEmptyList<String>, Deck> = either {
    accumulate {
        ensureOrAccumulate(cards.size < 60) { "Too many cards" }
        ensureOrAccumulate(cards.size > 60) { "More cards needed" }
        ensureOrAccumulate(title.isNotBlank()) { "Title cannot be blank" }
    }
    this@validate
}