package com.example.myapplication.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "首页")
    data object Add : Screen("add", "记录鱼获")
    data object Records : Screen("records", "鱼获列表")
    data object Stats : Screen("stats", "数据统计")
    data object Detail : Screen("detail/{recordId}", "鱼获详情") {
        fun createRoute(recordId: Long) = "detail/$recordId"
    }
    data object Edit : Screen("edit/{recordId}", "编辑记录") {
        fun createRoute(recordId: Long) = "edit/$recordId"
    }
}
