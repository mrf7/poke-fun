package deck

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.core.PickerType
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import tcg.Deck
import tcg.DeckError
import tcg.MultipleCards
import utils.VerticalSplitPaneSplitter

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeckPane(
    deck: DeckViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        TopAppBar(
            title = {
                BasicTextField(
                    deck.deck.title,
                    onValueChange = { deck.apply(DeckOperation.ChangeTitle(it)) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary),
                    singleLine = true,
                )
            },
            actions = {
                IconButton(
                    onClick = { deck.apply(DeckOperation.Clear) }
                ) { Icon(Icons.Default.Delete, contentDescription = "Clear") }

                val openPicker = rememberFilePickerLauncher(
                    type = PickerType.File(extensions = listOf("deck"))
                ) { file ->
                    /* what to do with the chosen file */
                }
                IconButton(
                    onClick = { openPicker.launch() },
                    enabled = false
                ) { Icon(Icons.Default.FileOpen, contentDescription = "Open") }

                val savePicker = rememberFileSaverLauncher { file ->
                    /* what to do with the chosen file */
                }
                IconButton(
                    onClick = { savePicker.launch(baseName = deck.deck.title, extension = "deck") },
                    enabled = false,
                ) { Icon(Icons.Default.Save, contentDescription = "Save") }

                VerticalDivider()
                IconButton(
                    onClick = { },
                    enabled = false
                ) { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo") }
                IconButton(
                    onClick = { deck.undo() },
                    enabled = true
                ) { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo") }
            }
        )
        VerticalSplitPane(
            splitPaneState = rememberSplitPaneState(1.0f),
            modifier = Modifier.fillMaxSize().padding(5.dp)
        ) {
            first {
                // TODO change the deck sort to group evolution groups together
                MultipleCards(
                    cards = deck.deck.cards.sorted(),
                    problems = deck.problems ?: emptyList(),
                    modifier = Modifier.fillMaxSize()
                ) { card ->
                    TextButton(
                        onClick = { deck.apply(DeckOperation.RemoveCard(card)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Add ${card.name}")
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            second(60.dp) {
                when (val problems = deck.problems) {
                    null -> DeckProblemLine("Everything is fine :)", fontStyle = FontStyle.Italic)
                    else -> DeckProblems(
                        problems,
                        Modifier.background(MaterialTheme.colorScheme.background)
                    )
                }
            }
            splitter {
                VerticalSplitPaneSplitter()
            }
        }
    }
}

@Composable
fun DeckProblems(problems: List<DeckError>, modifier: Modifier = Modifier) {
    Surface(modifier) {
        Box(modifier) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.verticalScroll(scrollState).fillMaxSize()
            ) {
                for (problem in problems) {
                    DeckProblemLine(problem.message)
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        }
    }
}

@Composable
fun DeckProblemLine(problem: String, fontStyle: FontStyle? = null, modifier: Modifier = Modifier) {
    Text(problem, fontStyle = fontStyle, modifier = modifier.padding(2.dp))
}
