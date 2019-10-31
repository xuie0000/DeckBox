package com.r0adkll.deckbuilder.arch.data.features.preview

import com.google.gson.Gson
import com.jakewharton.rxrelay2.BehaviorRelay
import com.r0adkll.deckbuilder.arch.domain.features.preview.PreviewRepository
import com.r0adkll.deckbuilder.arch.domain.features.remote.model.ExpansionPreview
import io.reactivex.Observable
import java.io.IOException

class TestPreviewRepository : PreviewRepository {

    private val gson = Gson()
    private val testingPreviewJson = "{\"version\":2,\"expiresAt\":\"2019-03-01T12:00Z\",\"code\":\"sm9\",\"preview\":{\"logo\":{\"url\":\"https://images.pokemontcg.io/sm9/logo.png\",\"margins\":{\"top\":16}},\"title\":\"Team Up is here!\",\"description\":\"Over 180 cards! \\n6 brand-new TAG TEAM Pokémon-GX\\n6 more Pokémon-GX\\n4 Prism Star cards\\nMore than 25 Trainer cards\",\"textColor\":\"#FFFFFF\",\"background\":[{\"source\":{\"type\":\"base64\",\"value\":\"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\",\"density\":2},\"tile\":{\"x\":\"repeat\",\"y\":\"repeat\"}}],\"foreground\":{\"source\":{\"type\":\"url\",\"value\":\"https://firebasestorage.googleapis.com/v0/b/deck-builder-1b711.appspot.com/o/sm9_foreground_2.png?alt=media&token=f980f76c-f421-4abc-88be-cf1652c35025\"},\"aspectRatio\":true}}}"
    private var dismissed = BehaviorRelay.createDefault(false)

    override fun getExpansionPreview(): Observable<ExpansionPreview> {
        return dismissed.switchMap {
            if (!it) {
                val preview = gson.fromJson(testingPreviewJson, ExpansionPreview::class.java)
                Observable.just(preview)
            } else {
                Observable.error(IOException("No preview found"))
            }
        }
    }

    override fun dismissPreview() {
        dismissed.accept(true)
    }
}
