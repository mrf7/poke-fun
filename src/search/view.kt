package search

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import deck.DeckViewModel
import tcg.MultipleCards

@Composable
fun SearchPane(
    deck: DeckViewModel,
    search: SearchViewModel = viewModel<SearchViewModel>(),
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        Column {
            TextField(
                value = search.options.text,
                onValueChange = search::updateText,
                label = { Text("Card name", fontSize = 10.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search by card name") },
                modifier = Modifier.fillMaxWidth()
            )
            when (val result = search.result) {
                is SearchStatus.Loading ->
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp).padding(10.dp)
                    )

                is SearchStatus.Error ->
                    Text(
                        "Problems during search\n${result.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(10.dp)
                    )

                is SearchStatus.Ok -> when {
                    result.isEmpty -> Text(
                        "No match found",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )

                    else -> MultipleCards(
                        cards = result.results,
                        modifier = Modifier.fillMaxSize()
                    ) { card ->
                        TextButton(
                            onClick = { deck.add(card) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add ${card.name}")
                            Text("Add", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}