# Pigmee Tech（猪咪科技）

猪咪玩偶、猪咪建材、染料基底与彩虹猪咪换色的独立模组（从 1.21.1 猪咪科技移植，上游 AE2LT）。

- **Mod ID**：`pigtech`
- **运行环境**：Minecraft 1.12.2 + Cleanroom Loader
- **依赖**：**AE2-Supergiant**（必需，染料基底配方使用 `ae2:silicon`）；JEI 可选

## 内容

| 分类 | 内容 |
|---|---|
| 猪咪 | 猪咪 / 创造猪咪 / 彩虹猪咪（可戴在头上、可当合成催化剂） |
| 16 色猪咪 | 单方块 16 色变体（metadata 0-15）：任意猪咪 + 对应染料 → 该颜色；水桶洗回普通猪咪 |
| 建材 | 猪咪建材方块 + 16 色素板/框板（CTM 连纹）+ 16 色素/框台阶 |
| 染料 | 染料基底（黏土 + AE2 硅）+ 16 色染料配方 |
| 换色 | 彩虹猪咪与可染色物品合成循环 16 色；四件同物品按染料配方一次染四件 |
| 染料循环 | 普通猪咪 + 白色染料 = 白色猪咪；白色 + 黑色染料 = 黑色猪咪 …… 任意颜色都可继续用染料换色 |

## 构建 / 运行

```bash
export JAVA_HOME="C:/Users/Administrator/.jdks/corretto-25.0.2"   # JDK 25
./gradlew build          # build/libs/PigmeeTech.jar
./gradlew runClient      # 开发客户端（run/client）
```

- 需要 AE2：把 `AppliedEnergistics2-Supergiant-1.1.0+a47dfe029.jar` 放进 `run/client/mods`（服务端同理 `run/server/mods`），JEI 可选。
- 本目录是**独立的 Gradle 工程**（不是父项目的子模块），与仓库里其他工程互不影响。

## 旧存档

本模组使用全新的注册名（`pigtech:*`），**不复用任何旧模组的注册名**。拆分前旧世界里的猪咪内容不会自动迁移，建议用创造模式重新放置。

## 许可

- **源代码**：LGPL-3.0-or-later（见 `LICENSE`）
- **贴图与模型**：CC BY-NC-SA 3.0（来自 AE2LT，见 `LICENSE_ASSETS.md`）
