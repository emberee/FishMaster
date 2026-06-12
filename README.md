# 🎣 鱼获大师 - Fish Catch Recorder

> 一款专为钓鱼人设计的 Android 鱼获记录 App，支持路亚佬快速记录每次作钓的鱼种、照片、钓点、假饵等信息，并提供丰富的数据统计。

## 📱 截图

| 首页 | 记录鱼获 | 数据统计 |
|------|---------|---------|
| ![首页](screenshots/home.png) | ![记录](screenshots/add.png) | ![统计](screenshots/stats.png) |

## ✨ 功能特性

### 🏠 首页 Dashboard
- 累计鱼获总数、今日/本周/本月快速统计
- 热门鱼种排行、最近鱼获展示、最新动态列表
- 动态波浪背景动画

### 📸 记录鱼获
- **拍照 / 相册选择** — 记录鱼获照片
- **14种常见鱼种** — 路亚目标鱼（白条、马口、青稍、鲈鱼、黑鱼等）排在最前
- **自动定位** — 获取 GPS 坐标并逆地理编码为地址名称
- **🎣 路亚假饵选择器** — 17种常见假饵芯片点选（飞蝇钩、根钓钩、铁板、米诺、铅笔、VIB、亮片、软虫等），支持自定义输入
- 天气、重量、体长、备注等信息记录

### 📋 鱼获列表
- 按鱼种筛选、按时间/重量排序
- 关键词搜索（鱼种名、钓点、备注）
- 信息卡片展示（照片缩略图、鱼种、重量、体长、天气）

### 📊 数据统计
- **饼图** — Canvas 绘制的鱼种分布图（含动画）
- **重量排行** — 各鱼种累计重量条形图
- **🏆 大鱼榜 TOP 10** — 单尾最重排行

### 🔍 鱼获详情
- 满屏大图展示
- 完整信息查看、编辑、删除（含确认弹窗）

## 🛠 技术栈

| 技术 | 用途 |
|------|------|
| **Kotlin** | 开发语言 |
| **Jetpack Compose + Material3** | 现代声明式 UI |
| **Room + KSP** | 本地数据库 |
| **Navigation Compose** | 页面路由 + 底部导航 |
| **Coil** | 图片加载 |
| **ViewModel + Flow** | MVVM 数据驱动 |
| **LocationManager + Geocoder** | GPS 定位 + 逆地理编码 |
| **FileProvider** | 安全拍照 URI |

## 🐟 预置鱼种

按路亚作钓频率排序：
白条 🐟 · 马口鱼 🐟 · 青稍 🐟 · 鲫鱼 🐟 · 鲈鱼 🐠 · 黑鱼 🐡 · 翘嘴 🐟 · 鳊鱼 🐟 · 红尾 🐟 · 鲤鱼 🐟 · 草鱼 🐠 · 青鱼 🐠 · 鲶鱼 🐡 · 罗非鱼 🐠 · 黄辣丁 🐡 · 其他鱼类 🐠

## 🎣 路亚假饵列表

飞蝇钩-腹节摇蚊 · 飞蝇钩-格里菲斯 · 飞蝇钩-双钩响尾蛇 · 根钓钩-针尾 · 根钓钩-卷尾 · 铁板 · 米诺 · 铅笔 · 波爬 · VIB · 亮片 · 软虫-T尾 · 软虫-叉尾 · 软虫-面条虫 · 虾型软饵 · 雷蛙 · 胡须佬

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17+
- Android SDK 34+

### 构建步骤

```bash
# 1. 克隆项目
git clone https://github.com/yourusername/FishMaster.git
cd FishMaster

# 2. 用 Android Studio 打开 Android/ 目录

# 3. 构建 Debug APK
./gradlew :app:assembleDebug

# 4. 构建 Release APK（需先配置签名）
./gradlew :app:assembleRelease
```

### 直接安装

从 [Releases](https://github.com/emberee/FishMaster/releases/tag/V1.0.0) 页面下载最新 APK 直接安装到手机。

## 📦 项目结构

```
app/src/main/java/com/example/myapplication/
├── data/
│   ├── FishRecord.kt       # 鱼获记录实体
│   ├── FishSpecies.kt      # 鱼种实体 + 预设数据
│   ├── FishDao.kt          # Room DAO
│   ├── FishDatabase.kt     # Room 数据库
│   └── FishRepository.kt   # 数据仓库
├── viewmodel/
│   └── FishViewModel.kt    # 主 ViewModel
├── navigation/
│   └── NavGraph.kt         # 路由定义
├── ui/
│   ├── home/               # 首页
│   ├── add/                # 记录鱼获页
│   ├── records/            # 鱼获列表页
│   ├── stats/              # 数据统计页
│   ├── detail/             # 鱼获详情页
│   └── theme/              # 主题配色
└── MainActivity.kt         # 主入口 + 导航
```

## 🤝 贡献指南

欢迎 Fork 和 PR！如果有好的建议或发现了 bug，请提 [Issue](https://github.com/yourusername/FishMaster/issues)。

### 想加的 feature 列表
- [ ] 地图模式展示钓点
- [ ] 导出 Excel/CSV 数据
- [ ] 鱼获照片自动备份
- [ ] 潮汐/天气信息接入
- [ ] 称重记录曲线图
- [ ] 多语言支持

## 📄 开源协议

本项目基于 **MIT License** 开源。

---

**🎣 祝每次出钓都有好鱼获！**
