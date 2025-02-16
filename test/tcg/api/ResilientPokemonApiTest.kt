package tcg.api

import arrow.core.Either
import arrow.core.right
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import tcg.Card
import kotlin.test.Test
import kotlin.test.assertEquals

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
}