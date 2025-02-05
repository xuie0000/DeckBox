package app.deckbox.decks.impl.export

import app.deckbox.core.model.Card
import app.deckbox.core.model.Deck
import app.deckbox.core.model.Stacked
import app.deckbox.core.model.SuperType
import app.deckbox.features.cards.public.CardRepository
import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject

@Inject
class TextConverter(
  private val cardRepository: CardRepository,
) {

  suspend fun convert(deck: Deck): String {
    return buildString {
      appendLine("***** ${deck.name} *****")
      appendLine()

      val cards = cardRepository.observeCardsForDeck(deck.id).first()

      val pokemon = cards.filter { it.card.supertype == SuperType.POKEMON }
      appendLine("## Pokemon - ${pokemon.size}")
      appendLine()
      pokemon.forEach {
        appendPokemonCard(it)
      }
      appendLine()

      val trainers = cards.filter { it.card.supertype == SuperType.TRAINER }
      appendLine("## Trainers - ${trainers.size}")
      appendLine()
      trainers.forEach {
        appendPokemonCard(it)
      }
      appendLine()

      val energy = cards.filter { it.card.supertype == SuperType.ENERGY }
      appendLine("## Energy - ${energy.size}")
      appendLine()
      energy.forEach {
        appendPokemonCard(it)
      }
      appendLine()

      appendLine("Total Cards - ${cards.size}")
      appendLine()

      append("***** Generated by Deckbox - https://deckboxtcg.app/ *****")
    }
  }

  private fun StringBuilder.appendPokemonCard(it: Stacked<Card>) {
    appendLine("* ${it.count} ${it.card.name} ${it.card.expansion.ptcgoCode} ${it.card.number}")
  }
}
