package com.example.myapplication.ui.records

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.FishRecord
import com.example.myapplication.data.FishSpecies
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.FishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    viewModel: FishViewModel,
    onRecordClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val speciesList by viewModel.allSpecies.collectAsState()

    // 筛选状态
    var selectedSpeciesId by remember { mutableStateOf<Long?>(null) }
    var sortByNewest by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // 筛选和排序
    val filteredRecords = remember(allRecords, selectedSpeciesId, sortByNewest, searchQuery) {
        var list = allRecords

        if (selectedSpeciesId != null) {
            list = list.filter { it.speciesId == selectedSpeciesId }
        }

        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase()
            list = list.filter { record ->
                val species = speciesList.find { it.id == record.speciesId }
                species?.name?.lowercase()?.contains(query) == true ||
                        record.address.lowercase().contains(query) ||
                        record.notes.lowercase().contains(query)
            }
        }

        if (sortByNewest) {
            list.sortedByDescending { it.catchTime }
        } else {
            list.sortedByDescending { it.weight ?: 0.0 }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("鱼获列表") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 搜索栏
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("🔍 搜索鱼种、地点...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "清除")
                        }
                    }
                }
            )

            // 分类快速筛选
            SpeciesFilterRow(
                speciesList = speciesList,
                selectedId = selectedSpeciesId,
                onSelect = {
                    selectedSpeciesId = if (it == selectedSpeciesId) null else it
                }
            )

            // 排序切换
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "共 ${filteredRecords.size} 条记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "排序: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilterChip(
                        selected = sortByNewest,
                        onClick = { sortByNewest = true },
                        label = { Text("最新") },
                        leadingIcon = {
                            if (sortByNewest) Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = !sortByNewest,
                        onClick = { sortByNewest = false },
                        label = { Text("最重") },
                        leadingIcon = {
                            if (!sortByNewest) Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎣", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedSpeciesId != null || searchQuery.isNotEmpty())
                                "没有找到匹配的鱼获"
                            else
                                "还没有任何鱼获记录\n快去钓鱼吧！",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        EnhancedRecordCard(
                            record = record,
                            species = speciesList.find { it.id == record.speciesId },
                            viewModel = viewModel,
                            onClick = { onRecordClick(record.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SpeciesFilterRow(
    speciesList: List<FishSpecies>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedId == null,
                onClick = { selectedId?.let { onSelect(-1) } },
                label = { Text("全部") },
                leadingIcon = {
                    Text("🎣", fontSize = 14.sp)
                }
            )
        }
        items(speciesList) { species ->
            FilterChip(
                selected = selectedId == species.id,
                onClick = { onSelect(species.id) },
                label = { Text(species.name) },
                leadingIcon = {
                    Text(species.emoji, fontSize = 14.sp)
                }
            )
        }
    }
}

@Composable
private fun EnhancedRecordCard(
    record: FishRecord,
    species: FishSpecies?,
    viewModel: FishViewModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 左侧：照片或Emoji
            if (record.photoUri != null) {
                AsyncImage(
                    model = record.photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = species?.emoji ?: "🐟", fontSize = 36.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = species?.name ?: "未知鱼种",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = species?.emoji ?: "",
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = viewModel.getFormattedDateShort(record.catchTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (record.address.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = record.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (record.weight != null) {
                        InfoChip(
                            text = viewModel.getFormattedWeight(record.weight),
                            color = OceanBlue
                        )
                    }
                    if (record.length != null) {
                        InfoChip(
                            text = viewModel.getFormattedLength(record.length),
                            color = Teal
                        )
                    }
                    if (record.weather.isNotEmpty()) {
                        InfoChip(
                            text = record.weather,
                            color = WarmAmber
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun InfoChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}