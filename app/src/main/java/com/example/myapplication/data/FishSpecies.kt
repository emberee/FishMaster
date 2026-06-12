package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fish_species")
data class FishSpecies(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String
)

object DefaultSpecies {
    // 路亚目标鱼种放前面！日常作钓频率高的优先
    val species = listOf(
        FishSpecies(name = "白条", emoji = "🐟"),
        FishSpecies(name = "马口鱼", emoji = "🐟"),
        FishSpecies(name = "青稍", emoji = "🐟"),
        FishSpecies(name = "鲫鱼", emoji = "🐟"),
        FishSpecies(name = "鲈鱼", emoji = "🐠"),
        FishSpecies(name = "黑鱼", emoji = "🐡"),
        FishSpecies(name = "翘嘴", emoji = "🐟"),
        FishSpecies(name = "鳊鱼", emoji = "🐟"),
        FishSpecies(name = "红尾", emoji = "🐟"),
        FishSpecies(name = "鲤鱼", emoji = "🐟"),
        FishSpecies(name = "草鱼", emoji = "🐠"),
        FishSpecies(name = "青鱼", emoji = "🐠"),
        FishSpecies(name = "鲶鱼", emoji = "🐡"),
        FishSpecies(name = "罗非鱼", emoji = "🐠"),
        FishSpecies(name = "黄辣丁", emoji = "🐡"),
        FishSpecies(name = "其他鱼类", emoji = "🐠"),
    )
}

/** 路亚常用假饵列表 */
object CommonLures {
    val lures = listOf(
        "飞蝇钩-腹节摇蚊",
        "飞蝇钩-格里菲斯",
        "飞蝇钩-双钩响尾蛇",
        "根钓钩-针尾",
        "根钓钩-卷尾",
        "铁板",
        "米诺",
        "铅笔",
        "波爬",
        "VIB",
        "亮片",
        "软虫-T尾",
        "软虫-叉尾",
        "软虫-面条虫",
        "虾型软饵",
        "雷蛙",
        "胡须佬",
    )
}
