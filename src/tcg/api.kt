package tcg

import kotlinx.coroutines.delay
import tcg.Card
import tcg.Category
import tcg.PokemonStage
import tcg.Type
import kotlin.time.Duration.Companion.seconds

interface PokemonApi {
    suspend fun search(name: String): List<Card>
}

class FakePokemonApi {
    suspend fun search(name: String): List<Card> {
        delay(3.seconds)
        return FAKE_CARDS.filter { name.contains(name, ignoreCase = true) }
    }
    
    companion object {
        val FAKE_CARDS: List<Card> = listOf(
            Card("Bulbasaur", "sv3pt5-1", Category.Pokemon(PokemonStage.Basic), Type.Grass),
            Card("Charmander", "sv3pt5-4", Category.Pokemon(PokemonStage.Basic), Type.Fire),
            Card("Squirtle", "sv3pt5-7", Category.Pokemon(PokemonStage.Basic), Type.Water),
            Card("Caterpie", "sv3pt5-10", Category.Pokemon(PokemonStage.Basic), Type.Grass),
        )
    }
}
