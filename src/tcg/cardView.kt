package tcg

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card as Material3Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import arrow.core.Nel
import kotlin.reflect.jvm.internal.impl.descriptors.DeserializedDeclarationsFromSupertypeConflictDataKey

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultipleCards(
    cards: List<Card>,
    modifier: Modifier = Modifier,
    problems: List<DeckError> = emptyList(),
    extra: @Composable ColumnScope.(Card) -> Unit = { }
) {
    val problemsByCard by derivedStateOf {
        problems.flatMap {
            when (it) {
                is DeckError.CannotEvolve -> listOf(it.card.name to it.message)
                is DeckError.TooManyCopies -> it.cards.map { card -> card.name to it.message }
                else -> emptyList()
            }
        }.groupBy({ it.first }, { it.second })
    }
    Box(modifier) {
        val scrollState = rememberScrollState()
        FlowRow(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
        ) {
            for (card in cards) {
                SingleCard(card, extra = extra, problem = problemsByCard.containsKey(card.name))
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
fun SingleCard(
    card: Card,
    modifier: Modifier = Modifier,
    problem: Boolean = false,
    extra: @Composable ColumnScope.(Card) -> Unit = { }
) {
    Material3Card(modifier = modifier.width(150.dp).padding(5.dp)) {
        Column {
            Text(
                card.name,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(2.dp),
                color = if (problem) MaterialTheme.colorScheme.error else Color.Unspecified
            )
            Image(
                painter = card.imageResource,
                contentDescription = card.identifier,
                modifier = Modifier.padding(5.dp)
            )
            extra(card)
        }
    }
}