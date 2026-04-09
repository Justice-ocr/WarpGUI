# WarpGUI Mod - 传送点图形界面模组

> 支持 Minecraft **1.21.8+** | Fabric Loader 0.16+ | Java 21

---

## 📦 功能特性

| 功能 | 说明 |
|------|------|
| 🗂 自动解析 `/warp list` | 自动拦截并解析服务器返回的传送点列表 |
| 🔍 实时搜索 | 输入关键字即时过滤传送点 |
| 🖱 点击传送 | 单击选中，双击/Enter 直接执行 `/warp tp {name}` |
| 📜 滚动列表 | 鼠标滚轮/方向键导航，支持大量传送点 |
| 🀄 中文界面 | 完整中文本地化 |
| ➕ 手动添加 | 支持手动录入传送点名称 |
| ⌨️ 快捷键 | 默认 **G 键** 打开界面（可在控制设置中修改） |

---

## 🚀 快速构建

### 环境要求
- **Java 21** (JDK)
- **网络连接**（首次构建需要下载依赖）

### 构建步骤

```bash
# 1. 进入项目目录
cd warp-gui-mod

# 2. Windows 用户
gradlew.bat build

# 3. Linux/macOS 用户
chmod +x gradlew
./gradlew build
```

构建完成后，JAR 文件位于：
```
build/libs/warp-gui-1.0.0.jar
```

### 安装方式
1. 将 JAR 文件放入 `.minecraft/mods/` 目录
2. 确保已安装 **Fabric Loader 0.16+** 和 **Fabric API**
3. 启动游戏

---

## 🎮 使用方法

1. 进入服务器后，按 **G 键**（可在 `控制设置 → WarpGUI` 中修改）
2. GUI 会自动发送 `/warp list` 并解析返回结果
3. 在搜索框输入关键字过滤
4. **单击**选中，**双击**或按 **Enter** 传送
5. 点击**刷新列表**重新获取服务器最新传送点

---

## 🔧 兼容的服务端Warp插件

模组内置多种格式解析器，兼容以下插件输出：

- **EssentialsX**: `Warps available: home, spawn, market`
- **CMI**: `- warpname` 或 `[warpname]`
- **MyWarp**: 逐行输出格式
- **自定义插件**: 逗号分隔格式

> 如果自动解析失败，可使用界面内的"手动添加"功能

---

## 📁 项目结构

```
warp-gui-mod/
├── build.gradle                          # 构建配置
├── gradle.properties                     # 版本配置（在此修改MC版本）
├── settings.gradle
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── src/main/
    ├── java/com/warpgui/
    │   ├── WarpGuiMod.java               # 主入口，注册快捷键
    │   ├── client/
    │   │   ├── WarpGuiScreen.java        # GUI 主界面
    │   │   └── WarpListManager.java      # Warp列表解析管理器
    │   └── mixin/
    │       └── ClientChatReceivedMixin.java  # 拦截聊天消息
    └── resources/
        ├── fabric.mod.json
        ├── warpgui.mixins.json
        └── assets/warpgui/lang/
            ├── zh_cn.json                # 中文语言
            └── en_us.json                # 英文语言
```

---

## 🔄 适配其他MC版本

修改 `gradle.properties` 中的以下值：

```properties
# 改成你需要的版本
minecraft_version=1.21.8
yarn_mappings=1.21.8+build.1
loader_version=0.16.14
fabric_version=0.119.0+1.21.8
```

查询正确的 mappings 版本：https://fabricmc.net/develop/

---

## ❓ 常见问题

**Q: 列表为空？**
A: 部分插件需要权限才能使用 `/warp list`。可用"手动添加"功能。

**Q: 传送命令格式不对？**  
A: 默认执行 `/warp tp {名称}`，如果你的服务器用的是 `/warp {名称}`，
   修改 `WarpGuiScreen.java` 第 `teleportToWarp` 方法中的命令格式。

**Q: 显示乱码？**  
A: 确保游戏语言设置为简体中文，或字体支持中文字符。

---

## 📄 许可证

MIT License - 自由使用和修改
