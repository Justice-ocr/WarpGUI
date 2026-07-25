# WarpGUI

**WarpGUI** 是一个 Minecraft 客户端 Fabric 模组，为 HAW（Home and Warp）服务端插件提供图形化传送点管理界面。

> 仅客户端，无需服务端安装。  
> 当前 monorepo 同时维护 Minecraft **1.21.4 / 1.21.8 / 1.21.11**。

## 仓库结构

```text
WarpGUI/
├── README.md
├── LICENSE
├── .gitignore
└── versions/
    ├── 1.21.4/    # Minecraft 1.21.4
    ├── 1.21.8/    # Minecraft 1.21.8
    └── 1.21.11/   # Minecraft 1.21.11
```

每个版本是**独立可构建**的 Fabric 工程。共享业务逻辑以 `1.21.8` 为基准对齐；仅保留版本相关差异（如按键绑定 API、`fabric.mod.json`、Loom/依赖版本）。

## 功能

- 按 `G` 打开传送点 GUI
- 自动解析 HAW 聊天列表
- 搜索、分页、键盘导航
- Warp / Home 双列表
- 收藏置顶、本地缓存、增量/自动刷新
- 多服务器配置与自动检测
- UI 主题：`vanilla` / `light`

## 版本对照

| Minecraft | 工程目录 | 构建产物 |
|-----------|----------|----------|
| 1.21.4 | `versions/1.21.4` | `warp-gui-2.0.1.jar` |
| 1.21.8 | `versions/1.21.8` | `warp-gui-2.0.1.jar` |
| 1.21.11 | `versions/1.21.11` | `warp-gui-2.0.1.jar` |

## 构建

**要求：** Java 21+、网络（首次下载依赖）

```bash
cd versions/1.21.8
# Windows
gradlew.bat build
# Linux/macOS
chmod +x gradlew && ./gradlew build
```

产物位于对应版本目录的 `build/libs/`。

## 分支策略

| 分支 | 说明 |
|------|------|
| `main` | **唯一开发主干**，包含全部版本目录 |
| `mc-1.21.4` / `mc-1.21.8` / `mc-1.21.11` | 兼容用别名，内容与 `main` 同步，不再单独演进 |

请在 `main` 上开发；跨版本改动时同步修改 `versions/*` 中的共享源码，并保留各版本 API 差异文件。

## 使用

1. 安装 Fabric Loader 与 Fabric API
2. 将对应版本 jar 放入 `.minecraft/mods/`
3. 进服后按 `G` 打开 GUI，点刷新抓取列表

默认快捷键与指令模板可在配置界面修改。

## License

MIT
