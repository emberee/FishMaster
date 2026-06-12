package com.example.myapplication.ui.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.SpeciesCount
import com.example.myapplication.data.SpeciesWeightSum
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.FishViewModel
import kotlin.math.atan2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: FishViewModel,
    onRecordClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val totalCount by viewModel.totalCount.collectAsState()
    val todayCount by viewModel.todayCount.collectAsState()
    val weekCount by viewModel.weekCount.collectAsState()
    val monthCount by viewModel.monthCount.collectAsState()
    val speciesCounts by viewModel.speciesCounts.collectAsState()
    val speciesWeightSum by viewModel.speciesWeightSum.collectAsState()
    val topHeavyRecords by viewModel.topHeavyRecords.collectAsState()
    val allSpecies by viewModel.allSpecies.collectAsState()

    val chartColors = listOf(
        OceanBlue, Teal, WarmAmber, SunsetOrange, ForestGreen,
        Color(0xFF9B5DE5), Color(0xFFF15BB5), Color(0xFF00BBF9),
        Color(0xFF00F5D4), Color(0xFFFEE440)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据统计") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 总览卡片
            item {
                OverviewCard(
                    totalCount = totalCount,
                    todayCount = todayCount,
                    weekCount = weekCount,
                    monthCount = monthCount
                )
            }

            // 鱼种分布饼图
            if (speciesCounts.isNotEmpty()) {
                item {
                    PieChartCard(
                        speciesCounts = speciesCounts,
                        allSpecies = allSpecies,
                        chartColors = chartColors
                    )
                }
            }

            // 重量排行
            if (speciesWeightSum.isNotEmpty()) {
                item {
                    WeightRankCard(
                        speciesWeightSum = speciesWeightSum,
                        allSpecies = allSpecies,
                        chartColors = chartColors
                    )
                }
            }

            // 大鱼榜
            if (topHeavyRecords.isNotEmpty()) {
                item {
                    TopHeavyCard(
                        records = topHeavyRecords,
                        viewModel = viewModel,
                        onRecordClick = onRecordClick
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun OverviewCard(
    totalCount: Int,
    todayCount: Int,
    weekCount: Int,
    monthCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(OceanBlue, Teal)
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎣 鱼获总览",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$totalCount",
                color = Color.White,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "累计总条数",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStatItem(value = "$todayCount", label = "今日")
                MiniStatItem(value = "$weekCount", label = "本周")
                MiniStatItem(value = "$monthCount", label = "本月")
            }
        }
    }
}

@Composable
private fun MiniStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun PieChartCard(
    speciesCounts: List<SpeciesCount>,
    allSpecies: List<com.example.myapplication.data.FishSpecies>,
    chartColors: List<Color>
) {
    val total = speciesCounts.sumOf { it.count }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "🐟 鱼种分布",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 饼图
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val animationProgress = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        animationProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(1500, easing = FastOutSlowInEasing)
                        )
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 40f
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        val topLeft = Offset(
                            center.x - radius,
                            center.y - radius
                        )

                        var startAngle = -90f
                        speciesCounts.take(8).forEachIndexed { index, sc ->
                            val sweep = (sc.count.toFloat() / total) * 360f * animationProgress.value
                            drawArc(
                                color = chartColors[index % chartColors.size],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += sweep
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$total",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "总计",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 图例
                Column(modifier = Modifier.weight(1f)) {
                    speciesCounts.take(8).forEachIndexed { index, sc ->
                        val species = allSpecies.find { it.id == sc.speciesId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(chartColors[index % chartColors.size])
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${species?.emoji ?: ""} ${species?.name ?: "未知"}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${sc.count}条",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (speciesCounts.size > 8) {
                        Text(
                            text = "... 还有 ${speciesCounts.size - 8} 种",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightRankCard(
    speciesWeightSum: List<SpeciesWeightSum>,
    allSpecies: List<com.example.myapplication.data.FishSpecies>,
    chartColors: List<Color>
) {
    val maxWeight = speciesWeightSum.maxOfOrNull { it.totalWeight } ?: 1.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "⚖️ 鱼种重量排行",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            speciesWeightSum.sortedByDescending { it.totalWeight }.forEachIndexed { index, sws ->
                val species = allSpecies.find { it.id == sws.speciesId }
                val progress = (sws.totalWeight / maxWeight).toFloat()
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(1000, delayMillis = index * 100)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (index < 3) chartColors[index] else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(24.dp)
                    )

                    Text(
                        text = "${species?.emoji ?: ""} ${species?.name ?: "未知"}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            chartColors[index % chartColors.size],
                                            chartColors[(index + 1) % chartColors.size]
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%.1fkg", sws.totalWeight),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(52.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun TopHeavyCard(
    records: List<com.example.myapplication.data.FishRecord>,
    viewModel: FishViewModel,
    onRecordClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🏆 大鱼榜 TOP 10",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = WarmAmber
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            records.forEachIndexed { index, record ->
                val species = viewModel.allSpecies.value.find { it.id == record.speciesId }
                val medal = when (index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> "${index + 1}."
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index < 3)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    onClick = { onRecordClick(record.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = medal,
                            fontSize = 20.sp,
                            modifier = Modifier.width(36.dp)
                        )
                        Text(
                            text = "${species?.emoji ?: ""} ${species?.name ?: "未知"}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(80.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.getFormattedWeight(record.weight),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (record.length != null) {
                                Text(
                                    text = "体长${viewModel.getFormattedLength(record.length)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = viewModel.getFormattedDateShort(record.catchTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}