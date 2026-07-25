# WarpGUI

**WarpGUI** 是一个 Minecraft 客户端 Fabric 模组，为 HAW（Home and Warp）服务端插件提供图形化传送点管理界面。

> 仅客户端，无需服务端安装。  
> 支持 Minecraft **1.21.4 / 1.21.8 / 1.21.11**。

## 分支策略

| 分支 | 内容 | 用途 |
|------|------|------|
| `main` | monorepo，包含全部版本源码 | 总览、对照、跨版本同步 |
| `mc-1.21.4` | **仅** 1.21.4 工程（仓库根目录） | 该版本开发 / 构建 / 发布 |
| `mc-1.21.8` | **仅** 1.21.8 工程（仓库根目录） | 该版本开发 / 构建 / 发布 |
| `mc-1.21.11` | **仅** 1.21.11 工程（仓库根目录） | 该版本开发 / 构建 / 发布 |

日常开发某个版本时，请切换到对应 `mc-*` 分支：

```bash
git checkout mc-1.21.8
gradlew.bat build   # Windows
./gradlew build     # Linux/macOS
```

跨版本同步逻辑时，在 `main` 的 `versions/*` 中对照修改，再分别同步到各 `mc-*` 分支。

## main 目录结构

```text
WarpGUI/
├── README.md
├── LICENSE
├── .gitignore
└── versions/
    ├── 1.21.4/
    ├── 1.21.8/
    └── 1.21.11/
```

## 功能

- 按 `G` 打开传送点 GUI
- 自动解析 HAW 聊天列表
- 搜索、分页、键盘导航
- Warp / Home 双列表
- 收藏置顶、本地缓存、增量/自动刷新
- 多服务器配置与自动检测
- UI 主题：`vanilla` / `light`

## 版本对照

| Minecraft | main 路径 | 版本分支 |
|-----------|-----------|----------|
| 1.21.4 | `versions/1.21.4` | `mc-1.21.4` |
| 1.21.8 | `versions/1.21.8` | `mc-1.21.8` |
| 1.21.11 | `versions/1.21.11` | `mc-1.21.11` |

## 在 main 上构建某个版本

```bash
cd versions/1.21.8
gradlew.bat build
```

## License

MIT
