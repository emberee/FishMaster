package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.add.AddRecordScreen
import com.example.myapplication.ui.detail.DetailScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.records.RecordsScreen
import com.example.myapplication.ui.stats.StatisticsScreen
import com.example.myapplication.ui.theme.FishTheme
import com.example.myapplication.viewmodel.FishViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FishTheme(dynamicColor = true) {
                FishApp()
            }
        }
    }
}

// ===== 底部导航项定义 =====
sealed class BottomNavItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(
        screen = Screen.Home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "首页"
    )
    data object Records : BottomNavItem(
        screen = Screen.Records,
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List,
        label = "记录"
    )
    data object Add : BottomNavItem(
        screen = Screen.Add,
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircle,
        label = "记录"
    )
    data object Stats : BottomNavItem(
        screen = Screen.Stats,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        label = "统计"
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Records,
    BottomNavItem.Add,
    BottomNavItem.Stats
)

@Composable
fun FishApp() {
    val navController = rememberNavController()
    val viewModel: FishViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 判断是否显示底部导航栏
    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        bottomNavItems.any { it.screen.route == dest.route }
    } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FishBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(tween(300)) { it } },
            exitTransition = { slideOutHorizontally(tween(300)) { -it / 3 } },
            popEnterTransition = { slideInHorizontally(tween(300)) { -it / 3 } },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } }
        ) {
            // 首页
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate(Screen.Add.route) },
                    onRecordClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    },
                    onRecordsClick = {
                        navController.navigate(Screen.Records.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onStatsClick = {
                        navController.navigate(Screen.Stats.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 添加记录
            composable(Screen.Add.route) {
                AddRecordScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 编辑记录
            composable(
                route = Screen.Edit.route,
                arguments = listOf(navArgument("recordId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
                AddRecordScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    editRecordId = recordId
                )
            }

            // 记录列表
            composable(Screen.Records.route) {
                RecordsScreen(
                    viewModel = viewModel,
                    onRecordClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // 统计页面
            composable(Screen.Stats.route) {
                StatisticsScreen(
                    viewModel = viewModel,
                    onRecordClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // 详情页面
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("recordId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
                DetailScreen(
                    viewModel = viewModel,
                    recordId = recordId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id ->
                        navController.navigate(Screen.Edit.createRoute(id))
                    }
                )
            }
        }
    }
}

@Composable
private fun FishBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == item.screen.route
            } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = true
            )
        }
    }
}