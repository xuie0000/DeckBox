package app.deckbox.shared.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.deckbox.common.compose.LocalWindowSizeClass
import app.deckbox.common.compose.PlatformBackHandler
import app.deckbox.common.compose.icons.DeckBoxIcons
import app.deckbox.common.compose.icons.filled.BoosterPack
import app.deckbox.common.compose.icons.filled.Browse
import app.deckbox.common.compose.icons.filled.Collection
import app.deckbox.common.compose.icons.filled.Decks
import app.deckbox.common.compose.icons.outline.BoosterPack
import app.deckbox.common.compose.icons.outline.Browse
import app.deckbox.common.compose.icons.outline.Collection
import app.deckbox.common.compose.icons.outline.Decks
import app.deckbox.common.compose.navigation.DetailNavigation
import app.deckbox.common.compose.navigation.LocalDetailNavigation
import app.deckbox.common.resources.strings.DeckBoxStrings
import app.deckbox.common.screens.BoosterPackScreen
import app.deckbox.common.screens.BrowseScreen
import app.deckbox.common.screens.DeckBoxScreen
import app.deckbox.common.screens.DecksScreen
import app.deckbox.common.screens.ExpansionsScreen
import app.deckbox.common.screens.RootScreen
import app.deckbox.common.screens.SettingsScreen
import app.deckbox.common.screens.UrlScreen
import app.deckbox.core.extensions.fluentIf
import app.deckbox.shared.navigator.MainDetailNavigator
import app.deckbox.shared.navigator.OpenUrlNavigator
import cafe.adriel.lyricist.LocalStrings
import com.moriatsushi.insetsx.navigationBars
import com.moriatsushi.insetsx.safeContentPadding
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.rememberOverlayHost
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration

@Composable
internal fun Home(
  backstack: SaveableBackStack,
  navigator: Navigator,
  windowInsets: WindowInsets,
  modifier: Modifier = Modifier,
) {
  val windowSizeClass = LocalWindowSizeClass.current
  val navigationType = remember(windowSizeClass) {
    NavigationType.forWindowSizeSize(windowSizeClass)
  }

  val detailBackStack = rememberSaveableBackStack(listOf(RootScreen()))
  val detailNavigator = rememberCircuitNavigator(detailBackStack) {
    detailBackStack.popUntil { false }
    detailBackStack.push(RootScreen())
  }
  val detailUrlNavigator = remember(detailNavigator) {
    OpenUrlNavigator(detailNavigator) { url -> navigator.goTo(UrlScreen(url)) }
  }

  val mainDetailNavigator = remember(navigator, detailUrlNavigator, navigationType) {
    MainDetailNavigator(
      mainNavigator = navigator,
      detailNavigator = detailUrlNavigator,
      isDetailEnabled = navigationType == NavigationType.RAIL ||
        navigationType == NavigationType.PERMANENT_DRAWER,
    )
  }

  val rootScreen by remember(backstack) {
    derivedStateOf { backstack.last().screen }
  }

  val rootDetailScreen by remember(detailBackStack) {
    derivedStateOf { detailBackStack.topRecord?.screen }
  }

  val currentPresentation by remember(backstack) {
    derivedStateOf {
      (backstack.topRecord?.screen as? DeckBoxScreen)?.presentation
    }
  }

  val strings = LocalStrings.current
  val navigationItems = remember { buildNavigationItems(strings) }

  val overlayHost = rememberOverlayHost()
  PlatformBackHandler(overlayHost.currentOverlayData != null) {
    overlayHost.currentOverlayData?.finish(Unit)
  }

  val detailNavigationState by remember {
    derivedStateOf {
      if (navigationType == NavigationType.BOTTOM_NAVIGATION) return@derivedStateOf DetailNavigation.None
      detailBackStack.topRecord?.screen
        ?.let { it as? DeckBoxScreen }
        ?.let { DetailNavigation.Current(it) }
        ?: DetailNavigation.Hidden
    }
  }

  CompositionLocalProvider(
    LocalDetailNavigation provides detailNavigationState,
  ) {
    ContentWithOverlays(
      overlayHost = overlayHost,
    ) {
      Scaffold(
        bottomBar = {
          if (navigationType == NavigationType.BOTTOM_NAVIGATION) {
            AnimatedVisibility(
              visible = currentPresentation?.hideBottomNav == false,
              enter = slideInVertically { it },
              exit = slideOutVertically { it },
            ) {
              HomeNavigationBar(
                selectedNavigation = rootScreen,
                navigationItems = navigationItems,
                onNavigationSelected = { mainDetailNavigator.resetRoot(it) },
                modifier = Modifier.fillMaxWidth(),
              )
            }
          } else {
            Spacer(
              Modifier
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .fillMaxWidth(),
            )
          }
        },
        // We let content handle the status bar
        contentWindowInsets = windowInsets,
        modifier = modifier,
      ) { paddingValues ->
        Row(
          modifier = Modifier
            .fillMaxSize()
            .fluentIf(
              navigationType == NavigationType.BOTTOM_NAVIGATION &&
                currentPresentation?.hideBottomNav != true,
            ) {
              padding(paddingValues)
            },
        ) {
          if (navigationType == NavigationType.RAIL) {
            HomeNavigationRail(
              selectedNavigation = rootScreen,
              navigationItems = navigationItems,
              onNavigationSelected = { mainDetailNavigator.resetRoot(it) },
              onCreateSelected = {
                // TODO: Nav to deck builder screen
              },
              modifier = Modifier.fillMaxHeight(),
            )
          } else if (navigationType == NavigationType.PERMANENT_DRAWER) {
            HomeNavigationDrawer(
              selectedNavigation = rootScreen,
              navigationItems = navigationItems,
              onNavigationSelected = { mainDetailNavigator.resetRoot(it) },
              modifier = Modifier.fillMaxHeight(),
            )
          }

          NavigableCircuitContent(
            navigator = mainDetailNavigator,
            backStack = backstack,
            decoration = GestureNavigationDecoration(
              onBackInvoked = mainDetailNavigator::pop,
            ),
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight(),
          )

          // TODO: Based on navigation type / screen width have a secondary CircuitContent here
          //  that certain Detail type screens can be pushed into if its available
          AnimatedVisibility(
            visible = rootDetailScreen !is RootScreen,
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight(),
          ) {
            NavigableCircuitContent(
              navigator = detailUrlNavigator,
              backStack = detailBackStack,
              unavailableRoute = { _, _ ->
                // Do nothing here
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HomeNavigationBar(
  selectedNavigation: Screen,
  navigationItems: List<HomeNavigationItem>,
  onNavigationSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier,
) {
  NavigationBar(
    modifier = modifier,
    windowInsets = WindowInsets.navigationBars,
  ) {
    for (item in navigationItems) {
      NavigationBarItem(
        icon = {
          HomeNavigationItemIcon(
            item = item,
            selected = selectedNavigation == item.screen,
          )
        },
        label = { Text(text = item.label) },
        selected = selectedNavigation == item.screen,
        onClick = { onNavigationSelected(item.screen) },
      )
    }
  }
}

@Composable
private fun HomeNavigationRail(
  selectedNavigation: Screen,
  navigationItems: List<HomeNavigationItem>,
  onNavigationSelected: (Screen) -> Unit,
  onCreateSelected: () -> Unit,
  modifier: Modifier = Modifier,
) {
  NavigationRail(
    modifier = modifier,
    header = {
      FloatingActionButton(
        onClick = onCreateSelected,
      ) {
        Icon(
          Icons.Rounded.Create,
          contentDescription = null,
        )
      }
    },
  ) {
    for (item in navigationItems) {
      NavigationRailItem(
        icon = {
          HomeNavigationItemIcon(
            item = item,
            selected = selectedNavigation == item.screen,
          )
        },
//        alwaysShowLabel = false,
        label = { Text(text = item.label) },
        selected = selectedNavigation == item.screen,
        onClick = { onNavigationSelected(item.screen) },
      )
    }

    Spacer(Modifier.weight(1f))
    NavigationRailItem(
      icon = {
        Icon(
          imageVector = Icons.Outlined.Settings,
          contentDescription = LocalStrings.current.settingsTabContentDescription,
        )
      },
      label = { Text(text = LocalStrings.current.settings) },
      selected = false,
      onClick = { onNavigationSelected(SettingsScreen()) },
    )
  }
}

@Composable
private fun HomeNavigationDrawer(
  selectedNavigation: Screen,
  navigationItems: List<HomeNavigationItem>,
  onNavigationSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .safeContentPadding()
      .padding(16.dp)
      .widthIn(max = 280.dp),
  ) {
    for (item in navigationItems) {
      @OptIn(ExperimentalMaterial3Api::class)
      NavigationDrawerItem(
        icon = {
          Icon(
            imageVector = item.iconImageVector,
            contentDescription = item.contentDescription,
          )
        },
        label = { Text(text = item.label) },
        selected = selectedNavigation == item.screen,
        onClick = { onNavigationSelected(item.screen) },
      )
    }
  }
}

@Composable
private fun HomeNavigationItemIcon(item: HomeNavigationItem, selected: Boolean) {
  if (item.selectedImageVector != null) {
    Crossfade(targetState = selected) { s ->
      Icon(
        imageVector = if (s) item.selectedImageVector else item.iconImageVector,
        contentDescription = item.contentDescription,
      )
    }
  } else {
    Icon(
      imageVector = item.iconImageVector,
      contentDescription = item.contentDescription,
    )
  }
}

@Immutable
private data class HomeNavigationItem(
  val screen: Screen,
  val label: String,
  val contentDescription: String,
  val iconImageVector: ImageVector,
  val selectedImageVector: ImageVector? = null,
)

internal enum class NavigationType {
  BOTTOM_NAVIGATION,
  RAIL,
  PERMANENT_DRAWER,
  ;

  companion object {
    fun forWindowSizeSize(windowSizeClass: WindowSizeClass): NavigationType = when {
      windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact -> BOTTOM_NAVIGATION
      windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact -> BOTTOM_NAVIGATION
      windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium -> RAIL
      else -> RAIL
    }
  }
}

private fun buildNavigationItems(strings: DeckBoxStrings): List<HomeNavigationItem> {
  return listOf(
    HomeNavigationItem(
      screen = DecksScreen(),
      label = strings.decks,
      contentDescription = strings.decksTabContentDescription,
      iconImageVector = DeckBoxIcons.Outline.Decks,
      selectedImageVector = DeckBoxIcons.Filled.Decks,
    ),
    HomeNavigationItem(
      screen = BoosterPackScreen(),
      label = strings.boosterPacks,
      contentDescription = strings.boosterPacksTabContentDescription,
      iconImageVector = Icons.Outlined.BoosterPack,
      selectedImageVector = Icons.Filled.BoosterPack,
    ),
    HomeNavigationItem(
      screen = ExpansionsScreen(),
      label = strings.expansions,
      contentDescription = strings.expansionsTabContentDescription,
      iconImageVector = DeckBoxIcons.Outline.Collection,
      selectedImageVector = DeckBoxIcons.Filled.Collection,
    ),
    HomeNavigationItem(
      screen = BrowseScreen(),
      label = strings.browse,
      contentDescription = strings.browseTabContentDescription,
      iconImageVector = DeckBoxIcons.Outline.Browse,
      selectedImageVector = DeckBoxIcons.Filled.Browse,
    ),
  )
}
