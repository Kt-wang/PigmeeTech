/*
 * Pigmee Tech - RainbowPigmeeColors (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.client.render.RainbowPigmeeColors.
 *
 * 纯客户端变色，无需服务端 tick / 方块更新：
 *   - 彩虹猪咪：HSV 色相循环（240 tick 一周期），整块染色
 *   - 命名为 jeb_ 的普通猪咪：仿原版 jeb_ 羊的 25-tick 渐变（调亮版）
 * 1.12.2 适配：IBlockColor/IItemColor 只在模型面声明了 tintindex 时生效，
 * 方块/物品模型面已补回源码模型自带的 "tintindex": 0。
 * 世界内渲染走 FumoTESR → BlockModelRenderer，同样消费 BlockColors。
 * 这里的整体变色是 shader 不可用时的降级路径；色带 shader 见 RainbowPigmeeShader。
 */
package pigtech.client.render;

import pigtech.Tags;
import pigtech.event.RainbowPigmeeHandler;
import pigtech.init.PigmeeBlocks;
import pigtech.tile.fumo.TileFumo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class RainbowPigmeeColors {

    private RainbowPigmeeColors() {
    }

    public static int currentColor() {
        return MathHelper.hsvToRGB((float) (animationTicks() % 240.0 / 240.0), 0.65F, 1.0F) & 0xFFFFFF;
    }

    /** 公开给 RainbowPigmeeSurface/TEISR 驱动 shader 的 AnimationTicks uniform */
    public static double animationTicks() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.world == null ? 0
                : mc.world.getTotalWorldTime() % 24000L + mc.getRenderPartialTicks();
    }

    /** 原版染料序列 + 25-tick 交叉淡化，调亮以适配深色猪咪贴图（对齐源码） */
    public static int sheepColor() {
        double ticks = animationTicks();
        int index = (int) (ticks / 25.0);
        float delta = (float) (ticks % 25.0 / 25.0);
        int from = brighten(toInt(EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(index % 16))));
        int to = brighten(toInt(EntitySheep.getDyeRgb(EnumDyeColor.byMetadata((index + 1) % 16))));
        return lerp(delta, from, to);
    }

    private static int toInt(float[] rgb) {
        return (int) (rgb[0] * 255.0F) << 16 | (int) (rgb[1] * 255.0F) << 8 | (int) (rgb[2] * 255.0F);
    }

    private static int lerp(float delta, int from, int to) {
        int r = (int) ((from >> 16 & 255) + ((to >> 16 & 255) - (from >> 16 & 255)) * delta);
        int g = (int) ((from >> 8 & 255) + ((to >> 8 & 255) - (from >> 8 & 255)) * delta);
        int b = (int) ((from & 255) + ((to & 255) - (from & 255)) * delta);
        return r << 16 | g << 8 | b;
    }

    /** 保留每种染料的色相；抬高明度下限，避免循环到接近纯黑（对齐源码） */
    private static int brighten(int color) {
        int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
        float scale = 255.0F / Math.max(1, Math.max(r, Math.max(g, b)));
        return 0xFF000000
                | Math.round(48 + r * scale * 207 / 255) << 16
                | Math.round(48 + g * scale * 207 / 255) << 8
                | Math.round(48 + b * scale * 207 / 255);
    }

    @SubscribeEvent
    public static void blockColors(ColorHandlerEvent.Block event) {
        BlockColors colors = event.getBlockColors();
        colors.registerBlockColorHandler((state, world, pos, tintIndex) -> currentColor(),
                PigmeeBlocks.RAINBOW_PIGMEE_FUMO);
        colors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return -1;
            }
            TileEntity te = world.getTileEntity(pos);
            return te instanceof TileFumo && RainbowPigmeeHandler.matchesName(((TileFumo) te).getCustomName())
                    ? sheepColor() : -1;
        }, PigmeeBlocks.PIGMEE_FUMO);
        // 16 色猪咪：颜色来自 TileFumo（物品渲染走 ItemColors，见下）
        colors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
            if (tintIndex != 0 || world == null || pos == null) {
                return -1;
            }
            TileEntity te = world.getTileEntity(pos);
            return te instanceof TileFumo ? dyeColor(((TileFumo) te).getColor()) : -1;
        }, PigmeeBlocks.PIGMEE_FUMO_COLORED);
    }

    @SubscribeEvent
    public static void itemColors(ColorHandlerEvent.Item event) {
        ItemColors colors = event.getItemColors();
        colors.registerItemColorHandler((stack, tintIndex) -> currentColor(),
                Item.getItemFromBlock(PigmeeBlocks.RAINBOW_PIGMEE_FUMO));
        colors.registerItemColorHandler((stack, tintIndex) ->
                        stack.hasDisplayName() && RainbowPigmeeHandler.matchesName(stack.getDisplayName())
                                ? sheepColor() : -1,
                Item.getItemFromBlock(PigmeeBlocks.PIGMEE_FUMO));
        colors.registerItemColorHandler((stack, tintIndex) ->
                tintIndex == 0 ? dyeColor(stack.getMetadata()) : -1,
                Item.getItemFromBlock(PigmeeBlocks.PIGMEE_FUMO_COLORED));
    }

    /**
     * EnumDyeColor metadata -> 原版染料 RGB（与羊毛/羊同色）。
     * 注意：不能再用 brighten() —— 它把最大通道归一到 255，会把灰/银/黑/棕冲成白/橙，
     * 导致颜色对不上；彩色猪咪使用灰度贴图 + 原始染料色相乘即可得到正确颜色。
     */
    public static int dyeColor(int meta) {
        if (meta < 0 || meta > 15) {
            return -1;
        }
        float[] rgb = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(meta));
        return ((int) Math.round(rgb[0] * 255.0F) << 16)
                | ((int) Math.round(rgb[1] * 255.0F) << 8)
                | (int) Math.round(rgb[2] * 255.0F);
    }
}
