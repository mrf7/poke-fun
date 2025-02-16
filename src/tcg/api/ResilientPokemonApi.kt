package tcg.api

import arrow.core.Either
import arrow.resilience.CircuitBreaker
import arrow.resilience.Schedule
import arrow.resilience.retry
import tcg.Card
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ResilientPokemonApi(
    private val inner: PokemonTcgApi,
    private val schedule: Schedule<Throwable, Any> = Schedule.exponential<Throwable>(250.milliseconds)
        .doUntil { _, dur -> dur > 5.seconds }
) : PokemonTcgApi {
    private val circuitBreaker = CircuitBreaker(
        openingStrategy = CircuitBreaker.OpeningStrategy.Count(4),
        resetTimeout = 2.seconds,
        exponentialBackoffFactor = 1.2,
        maxResetTimeout = 60.seconds,
    )

    override suspend fun search(name: String): Either<Throwable, List<Card>> {
        return schedule.retry {
            circuitBreaker.protectOrThrow {
                inner.search(name)
            }
        }
    }

    override suspend fun getById(identifier: String): Either<Throwable, Card?> {
        return schedule.retry {
            circuitBreaker.protectOrThrow {
                inner.getById(identifier)
            }
        }
    }
}