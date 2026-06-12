package com.example.myapplication.ui.add

import androidx.compose.material3.ExperimentalMaterial3Api
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.myapplication.data.FishRecord
import com.example.myapplication.data.FishSpecies
import com.example.myapplication.data.CommonLures
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.FishViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    viewModel: FishViewModel,
    onBack: () -> Unit,
    editRecordId: Long? = null
) {
    val context = LocalContext.current
    val speciesList by viewModel.allSpecies.collectAsState()

    // 编辑模式
    val isEdit = editRecordId != null
    var selectedSpeciesId by remember { mutableStateOf(speciesList.firstOrNull()?.id ?: 0L) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var weightText by remember { mutableStateOf("") }
    var lengthText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf("") }
    var baitUsed by remember { mutableStateOf("") }
    var catchTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var isLocating by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // 编辑模式下加载已有数据
    LaunchedEffect(editRecordId) {
        if (editRecordId != null) {
            viewModel.getRecordById(editRecordId) { record ->
                if (record != null) {
                    selectedSpeciesId = record.speciesId
                    photoUri = record.photoUri?.let { Uri.parse(it) }
                    address = record.address
                    latitude = record.latitude
                    longitude = record.longitude
                    weightText = record.weight?.toString() ?: ""
                    lengthText = record.length?.toString() ?: ""
                    notes = record.notes
                    weather = record.weather
                    baitUsed = record.baitUsed
                    catchTime = record.catchTime
                }
            }
        }
    }

    // 相机启动器
    val tempPhotoUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = tempPhotoUri.value
        }
    }

    // 相册选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoUri = uri
    }

    // 位置权限 — 只用 ACCESS_FINE_LOCATION，不加 BACKGROUND（会触发特殊权限弹窗导致拒绝）
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isLocating = true
            getLocation(context) { loc, addr ->
                latitude = loc.latitude
                longitude = loc.longitude
                address = addr
                isLocating = false
            }
        } else {
            Toast.makeText(context, "需要位置权限才能获取钓点位置", Toast.LENGTH_SHORT).show()
        }
    }

    // 相机权限
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createPhotoUri(context)
            tempPhotoUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    // 更新speciesId当speciesList加载后
    LaunchedEffect(speciesList) {
        if (speciesList.isNotEmpty() && selectedSpeciesId == 0L) {
            selectedSpeciesId = speciesList.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "编辑鱼获" else "记录新鱼获") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ===== 照片区域 =====
            Text(
                text = "📷 鱼获照片",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (photoUri != null) {
                Box {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "鱼获照片",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { photoUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "删除", tint = Color.White)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 拍照按钮
                    PhotoButton(
                        modifier = Modifier.weight(1f),
                        emoji = "📸",
                        label = "拍照",
                        gradient = listOf(OceanBlue, OceanLight),
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                val uri = createPhotoUri(context)
                                tempPhotoUri.value = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )
                    // 相册选择按钮
                    PhotoButton(
                        modifier = Modifier.weight(1f),
                        emoji = "🖼️",
                        label = "相册选择",
                        gradient = listOf(Teal, TealLight),
                        onClick = {
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== 鱼种选择 =====
            Text(
                text = "🐟 鱼种选择",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            SpeciesChipRow(
                speciesList = speciesList,
                selectedId = selectedSpeciesId,
                onSelect = { selectedSpeciesId = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ===== 位置信息 =====
            Text(
                text = "📍 钓点位置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (address.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = address,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 32.dp)
                        )
                    } else {
                        Text(
                            text = "点击下方按钮获取当前位置",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLocating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (address.isNotEmpty()) Teal else OceanBlue
                        )
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                if (address.isNotEmpty()) Icons.Default.Check else Icons.Default.MyLocation,
                                contentDescription = null
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (address.isNotEmpty()) "更新位置" else "获取当前位置"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== 时间和天气 =====
            Text(
                text = "⏰ 时间与天气",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.getFormattedDate(catchTime).take(16),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                WeatherSelector(weather = weather, onSelect = { weather = it })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== 体型数据 =====
            Text(
                text = "📏 体型数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("重量 (kg)") },
                    leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { lengthText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("体长 (cm)") },
                    leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 备注 =====
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("📝 备注") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ===== 路亚假饵选择 =====
            Text(
                text = "🎯 使用的假饵",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CommonLures.lures) { lure ->
                    val isSelected = baitUsed == lure
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            baitUsed = if (isSelected) "" else lure
                        },
                        label = { Text(lure, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            } else {
                                Text("🎣", fontSize = 12.sp)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 自定义假饵输入
            OutlinedTextField(
                value = if (baitUsed !in CommonLures.lures) baitUsed else "",
                onValueChange = { customBait ->
                    if (customBait.isNotEmpty()) baitUsed = customBait
                },
                label = { Text("✏️ 自定义假饵（如果上面没有你要的）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 保存按钮 =====
            Button(
                onClick = {
                    if (selectedSpeciesId == 0L) {
                        Toast.makeText(context, "请选择鱼种", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val weight = weightText.toDoubleOrNull()
                    val length = lengthText.toDoubleOrNull()

                    val record = FishRecord(
                        id = editRecordId ?: 0,
                        speciesId = selectedSpeciesId,
                        photoUri = photoUri?.toString(),
                        address = address,
                        latitude = latitude,
                        longitude = longitude,
                        catchTime = catchTime,
                        weight = weight,
                        length = length,
                        notes = notes,
                        weather = weather,
                        baitUsed = baitUsed,
                        createdAt = if (isEdit) System.currentTimeMillis() else System.currentTimeMillis()
                    )

                    if (isEdit) {
                        viewModel.updateRecord(record)
                        Toast.makeText(context, "已更新鱼获记录", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addRecord(record)
                        Toast.makeText(context, "🎉 记录成功！", Toast.LENGTH_SHORT).show()
                    }
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OceanBlue
                )
            ) {
                Icon(
                    if (isEdit) Icons.Default.Save else Icons.Default.Check,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEdit) "保存修改" else "记录鱼获！",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PhotoButton(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = emoji, fontSize = 36.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SpeciesChipRow(
    speciesList: List<FishSpecies>,
    selectedId: Long,
    onSelect: (Long) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(speciesList) { species ->
            val isSelected = species.id == selectedId
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = { onSelect(species.id) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = species.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = species.name,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WeatherSelector(weather: String, onSelect: (String) -> Unit) {
    val weatherOptions = listOf(
        "☀️ 晴天" to "晴天",
        "⛅ 多云" to "多云",
        "🌧️ 雨天" to "雨天",
        "🌫️ 雾天" to "雾天",
        "🌊 大风" to "大风"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedCard(
            modifier = Modifier
                .menuAnchor()
                .width(140.dp),
            shape = RoundedCornerShape(16.dp),
            onClick = { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (weather.isEmpty()) "🌤️ 天气" else weatherOptions.find { it.second == weather }?.first ?: weather,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            weatherOptions.forEach { (display, value) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getLocation(context: Context, callback: (Location, String) -> Unit) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(context, "请开启定位服务", Toast.LENGTH_SHORT).show()
            return
        }

        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            val geocoder = Geocoder(context, Locale.CHINA)
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val addressText = if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    listOfNotNull(
                        addr.adminArea,
                        addr.locality,
                        addr.subLocality,
                        addr.featureName
                    ).filter { it.isNotEmpty() }.joinToString(" ")
                } else {
                    "${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}"
                }
                callback(location, addressText)
            } catch (e: Exception) {
                callback(location, "位置获取成功 (${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)})")
            }
        } else {
            Toast.makeText(context, "无法获取位置，请确保GPS已开启", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "定位失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/** 使用 FileProvider 创建安全的拍照 URI，避免 Android 7.0+ 的 FileUriExposedException */
private fun createPhotoUri(context: Context): Uri {
    val photoFile = File(context.cacheDir, "fish_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}