package com.example.myapplication.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.FishRecord
import com.example.myapplication.data.FishSpecies
import com.example.myapplication.data.SpeciesCount
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.FishViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FishViewModel,
    onAddClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onRecordsClick: () -> Unit,
    onStatsClick: () -> Unit
) {
    val totalCount by viewModel.totalCount.collectAsState()
    val todayCount by viewModel.todayCount.collectAsState()
    val weekCount by viewModel.weekCount.collectAsState()
    val monthCount by viewModel.monthCount.collectAsState()
    val latestRecord by viewModel.latestRecord.collectAsState()
    val latestSpecies by viewModel.latestSpecies.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val speciesCounts by viewModel.speciesCounts.collectAsState()

    // 动画数值
    val animatedTotal = animateIntAsState(targetValue = totalCount, animationSpec = tween(1000))

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景渐变
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(OceanBlue, WaterEnd.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        ) {
            // 装饰性波纹
            WaveDecoration()
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 顶部标题区
            item {
                HeaderSection(
                    totalCount = animatedTotal.value,
                    onAddClick = onAddClick
                )
            }

            // 统计卡片
            item {
                StatsCardsRow(
                    todayCount = todayCount,
                    weekCount = weekCount,
                    monthCount = monthCount
                )
            }

            // 最近鱼获
            item {
                SectionTitle(
                    title = "最近鱼获",
                    action = "查看全部",
                    onActionClick = onRecordsClick
                )
            }

            item {
                if (latestRecord != null) {
                    LatestCatchCard(
                        record = latestRecord!!,
                        species = latestSpecies,
                        viewModel = viewModel,
                        onClick = { onRecordClick(latestRecord!!.id) }
                    )
                } else {
                    EmptyStateCard(
                        message = "还没有鱼获记录\n点击下方按钮开始记录吧！",
                        emoji = "🎣"
                    )
                }
            }

            // 热门鱼种
            item {
                SectionTitle(
                    title = "热门鱼种",
                    action = "查看统计",
                    onActionClick = onStatsClick
                )
            }

            item {
                SpeciesPopularityRow(
                    speciesCounts = speciesCounts,
                    viewModel = viewModel
                )
            }

            // 最近记录列表
            item {
                SectionTitle(title = "最新动态", action = null, onActionClick = {})
            }

            val recentRecords = allRecords.take(5)
            items(recentRecords) { record ->
                val species = viewModel.allSpecies.value.find { it.id == record.speciesId }
                RecordListItem(
                    record = record,
                    species = species,
                    viewModel = viewModel,
                    onClick = { onRecordClick(record.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun WaveDecoration() {
    val waveOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        waveOffset.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val waveHeight = 20f
        val path = Path()
        path.moveTo(0f, size.height)

        for (x in 0..size.width.toInt() step 5) {
            val y = size.height - waveHeight +
                    kotlin.math.sin((x + waveOffset.value) * 0.02f) * waveHeight * 0.5f +
                    kotlin.math.sin((x + waveOffset.value * 0.5f) * 0.03f) * waveHeight * 0.3f
            path.lineTo(x.toFloat(), y.toFloat())
        }
        path.lineTo(size.width, 0f)
        path.lineTo(0f, 0f)
        path.close()

        drawPath(path, Color.White.copy(alpha = 0.08f))
    }
}

@Composable
private fun HeaderSection(totalCount: Int, onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🎣 鱼获大师",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "记录每一次与鱼的邂逅",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 总计卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "累计鱼获",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$totalCount 条",
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                FilledTonalButton(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White,
                        contentColor = OceanBlue
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("记录", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatsCardsRow(todayCount: Int, weekCount: Int, monthCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "📅",
            label = "今日",
            count = todayCount,
            unit = "条",
            gradient = listOf(OceanBlue, OceanLight)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "📊",
            label = "本周",
            count = weekCount,
            unit = "条",
            gradient = listOf(Teal, TealLight)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "📈",
            label = "本月",
            count = monthCount,
            unit = "条",
            gradient = listOf(WarmAmber, SunsetOrange)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    count: Int,
    unit: String,
    gradient: List<Color>
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(gradient)
                )
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$count",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    action: String?,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (action != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = action,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LatestCatchCard(
    record: FishRecord,
    species: FishSpecies?,
    viewModel: FishViewModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji大图标
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = species?.emoji ?: "🐟",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = species?.name ?: "未知鱼种",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.getFormattedDate(record.catchTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (record.address.isNotEmpty()) {
                    Text(
                        text = "📍 ${record.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (record.weight != null) {
                    Text(
                        text = "⚖️ ${viewModel.getFormattedWeight(record.weight)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyStateCard(message: String, emoji: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SpeciesPopularityRow(
    speciesCounts: List<SpeciesCount>,
    viewModel: FishViewModel
) {
    if (speciesCounts.isEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("🐟", "🎣", "🐠", "🐡").forEach { emoji ->
                Card(
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 20.sp)
                    }
                }
            }
            Text(
                text = "还没有数据哦~",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        return
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val topSpecies = speciesCounts.take(7)
        items(topSpecies) { sc ->
            val species = viewModel.allSpecies.value.find { it.id == sc.speciesId }
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = species?.emoji ?: "🐟", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = species?.name ?: "未知",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "${sc.count}条",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordListItem(
    record: FishRecord,
    species: FishSpecies?,
    viewModel: FishViewModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = species?.emoji ?: "🐟", fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = species?.name ?: "未知鱼种",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Row {
                    Text(
                        text = viewModel.getFormattedDateShort(record.catchTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (record.weight != null) {
                        Text(
                            text = " · ${viewModel.getFormattedWeight(record.weight)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
