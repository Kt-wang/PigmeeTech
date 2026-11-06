# Pigmee Tech（猪咪科技）

猪咪玩偶、猪咪建材、染料基底与彩虹猪咪换色的独立模组 —— 把
[AE2 Lightning Tech](https://modrinth.com/mod/ae2-lightning-tech)（AE2LT，1.21.1）里的猪咪子系统
移植到 Minecraft 1.12.2 / Cleanroom。

> 与 AE2LT 作者、Applied Energistics: Crystal Science 作者和 Applied Energistics 2 团队均无隶属或背书关系。

## 基本信息

| 项目 | 值 |
|---|---|
| Mod ID | `pigtech` |
| Minecraft | 1.12.2 |
| 加载器 | [Cleanroom Loader](https://github.com/CleanroomMC/Cleanroom) `0.5.14-alpha`（**不能在普通 Forge 1.12.2 上运行**） |
| 前置（必需） | [AE2-Supergiant](https://github.com/FormlessDragon/Applied-Energistics-2-Supergiant) `v1.1.0` |
| 可选 | JEI `4.35.1`（彩虹猪咪信息页） |
| Java | 25（构建与运行） |

## 安装

1. 准备一个 Minecraft 1.12.2 + Cleanroom Loader `0.5.14-alpha` 的实例，并使用 JDK 25 启动。
2. 把 **AE2-Supergiant `v1.1.0`** 和 `PigmeeTech.jar` 一起放进 `mods/`。
3. 可选：加入 JEI `4.35.1`，可在 JEI 里查看彩虹猪咪信息页。
4. 启动游戏。染料基底的配方使用 `ae2:silicon`，缺少 AE2 时本模组不会加载。

## 内容

| 分类 | 内容 |
|---|---|
| 猪咪 | 猪咪 / 创造猪咪 / 彩虹猪咪（可戴在头上、可当合成催化剂） |
| 16 色猪咪 | 单方块 16 色变体（metadata 0-15）：任意猪咪 + 对应染料 → 该颜色；水桶洗回普通猪咪 |
| 建材 | 猪咪建材方块 + 16 色素板/框板（CTM 连纹）+ 16 色素/框台阶 |
| 染料 | 染料基底（黏土 + AE2 硅）+ 16 色染料配方 |
| 换色 | 彩虹猪咪与可染色物品合成循环 16 色；四件同物品按染料配方一次染四件 |
| 染料循环 | 普通猪咪 + 白色染料 = 白色猪咪；白色 + 黑色染料 = 黑色猪咪 …… 任意颜色都可继续用染料换色 |

具体配方以 JEI 为准。

## 构建

需要 **JDK 25**，Gradle Wrapper 已包含在仓库中：

```bash
./gradlew build      # 产物：build/libs/PigmeeTech.jar
./gradlew runClient  # 开发客户端（工作目录 run/client）
./gradlew runServer  # 开发服务端（工作目录 run/server）
```

- 开发运行前先把 AE2-Supergiant 放进 `run/client/mods`（服务端为 `run/server/mods`），JEI 可选。
- `run/`、`build/`、`.gradle/` 均不入库。

## 旧存档

本模组使用全新的注册名（`pigtech:*`），**不复用任何旧模组的注册名**。其它模组（包括 AE2LT 与
AE2 Crystal Science: Legacy）存档里已有的猪咪内容不会自动迁移，建议用创造模式重新放置。

## 许可与致谢

| 部分 | 许可 |
|---|---|
| 源代码 | [LGPL-3.0-or-later](LICENSES/LGPL-3.0.txt) |
| 贴图与模型 | [CC BY-NC-SA 3.0](LICENSES/CC-BY-NC-SA-3.0.txt) |

完整的版权、修改说明与第三方声明见 [LICENSE](LICENSE)、[LICENSE_ASSETS.md](LICENSE_ASSETS.md) 与 [NOTICE](NOTICE)。

**上游与致谢**

- **AE2 Lightning Tech（AE2LT，1.21.1）** —— 猪咪内容的最初来源：MOAKIEE、CystrySU、gjmhmm8、_leng；
  贴图 MHanHanBing；3D 模型 TedXenon。仓库：<https://modrinth.com/mod/ae2-lightning-tech>
- **Applied Energistics 2** —— AlgorithmX2 et al.（2013–2020）；贴图/模型 Ridanisaurus Rid。
  仓库：<https://github.com/AppliedEnergistics/Applied-Energistics-2>
- **AE2-Supergiant** —— 1.12.2 Cleanroom 分支：FormlessDragon。
  仓库：<https://github.com/FormlessDragon/Applied-Energistics-2-Supergiant>
- **1.12.2 移植与后续修改** —— KleinBlue。

贴图与模型不可用于商业用途；转载或修改需保留署名，并以相同许可共享。

## English

An unofficial Minecraft 1.12.2 / Cleanroom port of the pigmee subsystem from
[AE2 Lightning Tech](https://modrinth.com/mod/ae2-lightning-tech) (1.21.1): pigmee fumos, 16-color
pigmee variants, pigmee building blocks (panels, framed panels, slabs with CTM), dye base and the
rainbow pigmee recoloring loop.

- Mod id: `pigtech` — requires **AE2-Supergiant v1.1.0** and **Java 25**;
  it does **not** run on plain Forge 1.12.2. JEI 4.35.1 is optional.
- Build: `./gradlew build` → `build/libs/PigmeeTech.jar`; `./gradlew runClient` / `runServer` for development.
- License: source code **LGPL-3.0-or-later**, textures and models **CC BY-NC-SA 3.0**.
  See [LICENSE](LICENSE), [LICENSE_ASSETS.md](LICENSE_ASSETS.md) and [NOTICE](NOTICE).

Not affiliated with or endorsed by the AE2 Lightning Tech authors, the Applied Energistics: Crystal
Science authors, or the Applied Energistics 2 team.
