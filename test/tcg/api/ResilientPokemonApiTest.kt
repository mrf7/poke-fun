package tcg.api

import arrow.core.Either
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import tcg.Card
import tcg.Deck
import kotlin.test.Test

class ResilientPokemonApiTest {

    @Test
    fun test() = runTest {
        val fake = object : PokemonTcgApi {
            var searchCount = 0
            override suspend fun search(name: String): Either<Throwable, List<Card>> = Either.catch {
                if (searchCount == 4) return@catch emptyList<Card>()
                println("Failing $searchCount")
                searchCount++
                error("Failing $searchCount")
            }

            override suspend fun getById(identifier: String): Either<Throwable, Card?> {
                return Either.Right(null)
            }

        }
        val api = ResilientPokemonApi(fake)
        api.search("") shouldBe Either.Right(emptyList())
        api.search("") shouldBe Either.Right(emptyList())
    }

    @Test
    fun test2() {
        val cards = FakePokemonTcgApi.FakeCards
            .take(1)
            .flatMap { card -> List(5) { card } }

        Deck(cards = cards, title = "")

    }
}