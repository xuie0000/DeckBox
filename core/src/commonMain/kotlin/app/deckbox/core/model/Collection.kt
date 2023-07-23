package app.deckbox.core.model

class Collection(
  private val counts: Map<String, Count>,
) {

  fun getCount(card: Card): Int {
    TODO("Not yet implemented")
  }

  data class Count(
    val expansionId: String,
    val expansionName: String,
    val variationCounts: Map<String, Int>
  ) {

    /**
     * The total count across all variations
     */
    val totalCount: Int
      get() = variationCounts.values.sum()
  }
}
