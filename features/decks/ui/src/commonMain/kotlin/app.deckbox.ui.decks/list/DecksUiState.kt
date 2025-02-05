package app.deckbox.ui.decks.list

import androidx.compose.runtime.Stable
import app.deckbox.core.model.Deck
import app.deckbox.core.settings.DeckCardConfig
import app.deckbox.core.settings.SortOption
import app.deckbox.features.decks.public.ui.events.DeckCardEvent
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList

@Stable
data class DecksUiState(
  val isLoading: Boolean = false,
  val deckCardConfig: DeckCardConfig,
  val deckSortOrder: SortOption,
  val decks: ImmutableList<Deck>,
  val eventSink: (DecksUiEvent) -> Unit,
) : CircuitUiState

sealed interface DecksUiEvent : CircuitUiEvent {
  data object CreateNewDeck : DecksUiEvent
  data object OpenAppSettings : DecksUiEvent
  data object ImportDeck : DecksUiEvent
  data object ImportTournamentDeck : DecksUiEvent
  data class ChangeSortOrder(val sortOrder: SortOption) : DecksUiEvent
  data class CardEvent(val deck: Deck, val event: DeckCardEvent) : DecksUiEvent
}
