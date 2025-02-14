package tcg.api

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
            override suspend fun search(name: String): List<Card> {
                if (searchCount == 4) return emptyList()
                println("Failing $searchCount")
                searchCount++
                error("Failing $searchCount")
            }

            override suspend fun getById(identifier: String): Card? {
                return null
            }

        }
        val api = ResilientPokemonApi(fake)
        api.search("") shouldBe emptyList()
        api.search("") shouldBe emptyList()
    }
}