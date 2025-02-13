package tcg

import androidx.compose.runtime.Composable
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter

data class Deck(
    val title: String, val cards: List<Card>
) {
    companion object {
        val INITIAL: Deck = Deck("Awesome Deck", emptyList())
    }
}

sealed interface Card : Comparable<Card> {
    val name: String
    val identifier: String
    val category: Category
    val imageUrl: String
        get() {
            val (set, id) = identifier.split('-')
            return "https://images.pokemontcg.io/$set/${id}_hires.png"
        }
    val imageResource: AsyncImagePainter
        @Composable get() = rememberAsyncImagePainter(imageUrl)

    override fun compareTo(other: Card): Int {
        if (this.category != other.category) return this.category.compareTo(other.category)
        if (this.name != other.name) return this.name.compareTo(other.name)
        return this.identifier.compareTo(other.identifier)
    }
}

data class PokemonCard(
    override val name: String,
    override val identifier: String,
    override val category: Category.Pokemon,
    val type: Type.PokemonType
) : Card

data class EnergyCard(
    override val name: String,
    override val identifier: String,
    override val category: Category.Energy,
    val type: Type.EnergyType
) : Card

data class TrainerCard(
    override val name: String, override val identifier: String, override val category: Category.Trainer
) : Card

sealed interface Category : Comparable<Category> {
    data class Pokemon(val stage: PokemonStage) : Category
    data class Energy(val category: EnergyCategory) : Category
    data class Trainer(val category: TrainerCategory) : Category

    override fun compareTo(other: Category): Int = when {
        this is Pokemon && other is Pokemon -> this.stage.compareTo(other.stage)

        this is Pokemon -> -1
        other is Pokemon -> 1
        this is Energy && other is Energy -> this.category.compareTo(other.category)

        this is Energy -> -1
        other is Energy -> 1
        this is Trainer && other is Trainer -> this.category.compareTo(other.category)

        this is Trainer -> -1
        other is Trainer -> 1
        else -> 0
    }
}

enum class PokemonStage {
    Basic, Stage1, Stage2;
}

enum class EnergyCategory {
    Basic, Special;
}

enum class TrainerCategory {
    Item, Tool, Supporter, Stadium;
}

sealed interface Type {
    val imageUrl: String
    val imageResource: AsyncImagePainter
        @Composable get() = rememberAsyncImagePainter(imageUrl)

    sealed interface PokemonType : Type
    data object Dragon : PokemonType {
        override val imageUrl =
            "https://archives.bulbagarden.net/media/upload/thumb/8/8a/Dragon-attack.png/40px-Dragon-attack.png"
    }

    data object Colorless : PokemonType {
        override val imageUrl =
            "https://archives.bulbagarden.net/media/upload/thumb/1/1d/Colorless-attack.png/40px-Colorless-attack.png"
    }

    sealed interface EnergyType : Type

    data object Grass : PokemonType, EnergyType {
        override val imageUrl =
            "https://archives.bulbagarden.net/media/upload/thumb/2/2e/Grass-attack.png/40px-Grass-attack.png"
    }

    data object Water : PokemonType, EnergyType {
        override val imageUrl =
            "https://archives.bulbagarden.net/media/upload/thumb/1/11/Water-attack.png/40px-Water-attack.png"
    }

    data object Fire : Type, PokemonType, EnergyType {
        override val imageUrl =
            "https://archives.bulbagarden.net/media/upload/thumb/a/ad/Fire-attack.png/40px-Fire-attack.png"
    }

    data object Lightning : Type, PokemonType, EnergyType {
        override val imageUrl =
            "https://archives.bulbagarden.net/media/upload/thumb/0/04/Lightning-attack.png/40px-Lightning-attack.png"
    }

    data object Fighting : Type, PokemonType, EnergyType {
        override val imageUrl =
            "https://archives.bulbagarden.net/media/upload/thumb/4/48/Fighting-attack.png/40px-Fighting-attack.png"
    }

    data object Psychic : Type, PokemonType, EnergyType {
        override val imageUrl =
            "https://archives.bulbagarden.net/media/upload/thumb/e/ef/Psychic-attack.png/40px-Psychic-attack.png"
    }

    data object Darkness : Type, PokemonType, EnergyType {
        override val imageUrl =
            "https://archives.bulbagarden.net/media/upload/thumb/a/ab/Darkness-attack.png/40px-Darkness-attack.png"
    }

    data object Metal : Type, PokemonType, EnergyType {
        override val imageUrl =
            "https://archives.bulbagarden.net/media/upload/thumb/6/64/Metal-attack.png/40px-Metal-attack.png"
    }
}