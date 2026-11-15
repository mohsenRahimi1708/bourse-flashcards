package ir.bourse.flashcards.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.bourse.flashcards.R
import ir.bourse.flashcards.ui.manage.EditCardScreen
import ir.bourse.flashcards.ui.manage.ManageScreen
import ir.bourse.flashcards.ui.stats.StatsScreen
import ir.bourse.flashcards.ui.study.StudyScreen

object Routes {
    const val STUDY = "study"
    const val MANAGE = "manage"
    const val STATS = "stats"
    const val EDIT_ARG = "cardId"
    const val EDIT = "edit/{$EDIT_ARG}"

    fun edit(cardId: Long): String = "edit/$cardId"
}

private data class BottomTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

private val tabs = listOf(
    BottomTab(Routes.STUDY, R.string.tab_study, Icons.Outlined.School),
    BottomTab(Routes.MANAGE, R.string.tab_manage, Icons.AutoMirrored.Outlined.List),
    BottomTab(Routes.STATS, R.string.tab_stats, Icons.Outlined.BarChart),
)

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // The editor is a full-screen step, so it hides the tab bar.
            if (currentRoute != Routes.EDIT) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.switchTab(tab.route, currentRoute) },
                            icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.STUDY,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.STUDY) { StudyScreen() }

            composable(Routes.MANAGE) {
                ManageScreen(
                    onAddCard = { navController.navigate(Routes.edit(0L)) },
                    onEditCard = { cardId -> navController.navigate(Routes.edit(cardId)) },
                )
            }

            composable(Routes.STATS) { StatsScreen() }

            composable(
                route = Routes.EDIT,
                arguments = listOf(navArgument(Routes.EDIT_ARG) { type = NavType.LongType }),
            ) { entry ->
                EditCardScreen(
                    cardId = entry.arguments?.getLong(Routes.EDIT_ARG) ?: 0L,
                    onFinished = { navController.popBackStack() },
                )
            }
        }
    }
}

/** Tab switching keeps a single copy of each tab and restores its scroll state. */
private fun NavHostController.switchTab(route: String, currentRoute: String?) {
    if (currentRoute == route) return
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
