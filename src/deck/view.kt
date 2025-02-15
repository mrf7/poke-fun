package deck

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import tcg.DeckError
import tcg.MultipleCards
import utils.VerticalSplitPaneSplitter

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeckPane(
    deckViewModel: DeckViewModel,
    modifier: Modifier = Modifier
) {
    val deck by derivedStateOf { deckViewModel.deckState.deck }
    Column(modifier) {
        TopAppBar(
            title = {
                BasicTextField(
                    deck.title,
                    onValueChange = { deckViewModel.apply(DeckOperation.ChangeTitle(it)) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary),
                    singleLine = true,
                )
            },
            actions = {
                IconButton(onClick = { deckViewModel.reset() }) {
                    Icon(Icons.Default.AcUnit, contentDescription = "Reset")
                }
                IconButton(
                    onClick = { deckViewModel.apply(DeckOperation.Clear) }
                ) { Icon(Icons.Default.Delete, contentDescription = "Clear") }

                val openPicker = rememberFilePickerLauncher(
                    type = PickerType.File(extensions = listOf("deck"))
                ) { file ->
                    deckViewModel.loadFile(file)
                }
                IconButton(
                    onClick = { openPicker.launch() },
                    enabled = true
                ) { Icon(Icons.Default.FileOpen, contentDescription = "Open") }

                val savePicker = rememberFileSaverLauncher { file ->
                    deckViewModel.saveDeck(file)
                }
                IconButton(
                    onClick = { savePicker.launch(baseName = deck.title, extension = "deck") },
                    enabled = true,
                ) { Icon(Icons.Default.Save, contentDescription = "Save") }

                VerticalDivider()
                IconButton(
                    onClick = { deckViewModel.apply(DeckOperation.Undo) },
                    enabled = deckViewModel.deckState.hasUndo
                ) { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo") }
                IconButton(
                    onClick = { deckViewModel.apply(DeckOperation.Redo) },
                    enabled = deckViewModel.deckState.hasRedo
                ) { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo") }
            }
        )
        VerticalSplitPane(
            splitPaneState = rememberSplitPaneState(1.0f),
            modifier = Modifier.fillMaxSize().padding(5.dp)
        ) {
            first {
                // TODO change the deckState sort to group evolution groups together
                MultipleCards(
                    cards = deck.cards.sorted(),
                    problems = deckViewModel.problems ?: emptyList(),
                    modifier = Modifier.fillMaxSize()
                ) { card ->
                    TextButton(
                        onClick = { deckViewModel.apply(DeckOperation.RemoveCard(card)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Add ${card.name}")
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            second(60.dp) {
                when (val problems = deckViewModel.problems) {
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
