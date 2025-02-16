package tcg.api

import arrow.core.Either
import arrow.core.right
import deck.guard
import io.github.reactivecircus.cache4k.Cache
import tcg.Card

// would normally be a singleton ofc
private val cache = Cache.Builder<String, Card>().build()

class PokemonRepo(val api: PokemonTcgApi = ResilientPokemonApi(KtorPokemonTcgApi())) {
    suspend fun search(name: String): Either<Throwable, List<Card>> =
        api.search(name)
            .onRight { cards -> cards.onEach { cache.put(it.identifier, it) } }

    suspend fun getById(identifier: String): Either<Throwable, Card?> {
        val cached = cache.get(identifier)
        return if (cached != null) {
            cached.right()
        } else {
            api.getById(identifier).onRight {
                guard(it != null) { return@onRight }
                cache.put(it.identifier, it)
            }
        }
    }
}