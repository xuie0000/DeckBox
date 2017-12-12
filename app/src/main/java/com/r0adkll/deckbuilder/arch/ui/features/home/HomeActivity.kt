package com.r0adkll.deckbuilder.arch.ui.features.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.Menu
import android.view.MenuItem
import com.r0adkll.deckbuilder.R
import com.r0adkll.deckbuilder.arch.ui.components.BaseActivity
import com.r0adkll.deckbuilder.arch.ui.features.deckbuilder.DeckBuilderActivity
import com.r0adkll.deckbuilder.arch.ui.features.decks.DecksFragment
import com.r0adkll.deckbuilder.arch.ui.features.home.di.HomeComponent
import com.r0adkll.deckbuilder.arch.ui.features.home.di.HomeModule
import com.r0adkll.deckbuilder.arch.ui.features.importer.DeckImportActivity
import com.r0adkll.deckbuilder.arch.ui.features.settings.SettingsActivity
import com.r0adkll.deckbuilder.internal.analytics.Analytics
import com.r0adkll.deckbuilder.internal.analytics.Event
import com.r0adkll.deckbuilder.internal.di.AppComponent
import gov.scstatehouse.houseofcards.di.HasComponent
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : BaseActivity(), HasComponent<HomeComponent> {

    companion object {

        fun createIntent(context: Context): Intent = Intent(context, HomeActivity::class.java)
    }

    private lateinit var component: HomeComponent
    private lateinit var adapter: HomePagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        adapter = HomePagerAdapter(supportFragmentManager)
        pager.adapter = adapter
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val cards = DeckImportActivity.parseResults(resultCode, requestCode, data)
        cards?.let {
            if (it.isNotEmpty()) {
                Analytics.event(Event.SelectContent.Action("import_cards"))
                val intent = DeckBuilderActivity.createIntent(this, it)
                startActivity(intent)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_home, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_import -> {
                Analytics.event(Event.SelectContent.MenuAction("import_decklist"))
                DeckImportActivity.show(this)
                true
            }
            R.id.action_settings -> {
                Analytics.event(Event.SelectContent.MenuAction("settings"))
                startActivity(SettingsActivity.createIntent(this))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun setupComponent(component: AppComponent) {
        this.component = component.plus(HomeModule(this))
        this.component.inject(this)
    }


    override fun getComponent(): HomeComponent = component


    class HomePagerAdapter(
            fragmentManager: FragmentManager
    ) : FragmentPagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment? = when(position) {
            0 -> DecksFragment.newInstance()
            else -> null
        }


        override fun getCount(): Int = 1 // TODO: Increase when we add more screens
    }
}
