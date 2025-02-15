package tcg.api

import io.github.reactivecircus.cache4k.Cache
import tcg.Card

class PokemonRepo(val api: PokemonTcgApi = ResilientPokemonApi(KtorPokemonTcgApi())) {
    private val cache = Cache.Builder<String, Card>().build()
    suspend fun search(name: String): List<Card> = api.search(name)

    suspend fun getById(identifier: String): Card? {
        val cached = cache.get(identifier)
        return if (cached != null) {
            cached
        } else {
            api.getById(identifier)?.also { cache.put(it.identifier, it) }
        }
    }
}