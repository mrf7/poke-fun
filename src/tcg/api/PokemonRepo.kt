package tcg.api

import io.github.reactivecircus.cache4k.Cache
import tcg.Card

// would normally be a singleton ofc
private val cache = Cache.Builder<String, Card>().build()

class PokemonRepo(val api: PokemonTcgApi = ResilientPokemonApi(KtorPokemonTcgApi())) {
    suspend fun search(name: String): List<Card> =
        api.search(name).onEach { cache.put(it.identifier, it).also { println("Puttin in cache") } }

    suspend fun getById(identifier: String): Card? {
        val cached = cache.get(identifier)
        return if (cached != null) {
            cached
        } else {
            api.getById(identifier)?.also { cache.put(it.identifier, it) }
        }
    }
}