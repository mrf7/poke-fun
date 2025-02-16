package tcg.api

import arrow.core.Either
import arrow.core.right
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import tcg.Card
import tcg.Category
import tcg.PokemonCard
import tcg.PokemonStage
import tcg.Type

interface PokemonTcgApi {
    suspend fun search(name: String): Either<Throwable, List<Card>>
    suspend fun getById(identifier: String): Either<Throwable, Card?>
}

object FakePokemonTcgApi : PokemonTcgApi {
    override suspend fun search(name: String): Either<Throwable, List<Card>> {
        delay(3.seconds)
        return FakeCards.filter { name.contains(name, ignoreCase = true) }.right()
    }

    override suspend fun getById(identifier: String): Either<Throwable, Card?> = Either.catch {
        delay(1.seconds)
        FakeCards.firstOrNull { it.identifier == identifier }
    }


    val FakeCards: List<Card> = listOf(
        PokemonCard("Bulbasaur", "sv3pt5-1", Category.Pokemon(PokemonStage.Basic), Type.Grass),
        PokemonCard("Charmander", "sv3pt5-4", Category.Pokemon(PokemonStage.Basic), Type.Fire),
        PokemonCard("Squirtle", "sv3pt5-7", Category.Pokemon(PokemonStage.Basic), Type.Water),
        PokemonCard("Caterpie", "sv3pt5-10", Category.Pokemon(PokemonStage.Basic), Type.Grass),
    )
}
