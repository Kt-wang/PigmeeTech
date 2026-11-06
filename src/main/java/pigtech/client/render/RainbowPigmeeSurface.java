/*
 * Pigmee Tech - RainbowPigmeeSurface (1.12.2)
 * Port of ae2lt 1.21.1 client/render/RainbowPigmeeSurfaceLayer +
 * HyperdimensionalPigmeeTextureLayer（彩虹猪咪与超维猪咪共用的面部标记层）。
 *
 * 两个渲染通道（TESR 方块形态与 TEISR 物品形态共用）：
 *   1. 色带层：复用猪咪模型轮廓，逐顶点计算模型空间投影 UV / 深度 / 高度，
 *      交给 RainbowPigmeeShader 画出流动的彩虹像素色带（完全取代本体贴图）。
 *      顶点数学逐字对齐源码 vertex()：UV2 打包 depth/height × 4096。
 *   2. 标记层：同一组四边形，UV 从基础贴图精灵重映射到标记贴图精灵
 *      （两张 32×32 同布局贴图），全亮光照，画出眼睛等稀疏面部标记。
 * 方块/物品共用一份代码：state 为 null 即物品形态（深度参考朝向 NORTH）。
 */
package pigtech.client.render;

import pigtech.Tags;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;

public final class RainbowPigmeeSurface {

    /**
     * 色带层顶点格式：位置 + 颜色(携带朝向明暗) + UV0(面投影) + UV1(深度/高度×4096)。
     * 注意：第二个 UV 元素必须显式用 index=1 构造——直接复用 DefaultVertexFormats.TEX_2F
     * （index=0）会让两个元素都被映射到纹理单元 0，gl_MultiTexCoord1 永远为 (0,0)，
     * 色带退化成单一色。
     */
    public static final VertexFormat BAND_FORMAT = new VertexFormat()
            .addElement(DefaultVertexFormats.POSITION_3F)
            .addElement(DefaultVertexFormats.COLOR_4UB)
            .addElement(DefaultVertexFormats.TEX_2F)
            .addElement(new VertexFormatElement(1,
                    VertexFormatElement.EnumType.FLOAT,
                    VertexFormatElement.EnumUsage.UV, 2));

    private static final float SURFACE_OFFSET = 0.002F;
    private static final float MARKING_OFFSET = 0.004F;
    private static final long MODEL_SEED = 42L;

    private static TextureAtlasSprite baseSprite;
    private static TextureAtlasSprite markingSprite;

    private RainbowPigmeeSurface() {
    }

    // ==================== 通道 1：彩虹色带 ====================

    public static void renderBands(Tessellator tessellator, IBakedModel model, @Nullable IBlockState state,
                                   EnumFacing facing, float animationTicks) {
        BufferBuilder buffer = tessellator.getBuffer();
        RainbowPigmeeShader.bind();
        RainbowPigmeeShader.setAnimationTicks(animationTicks);
        buffer.begin(GL11.GL_QUADS, BAND_FORMAT);

        for (EnumFacing side : EnumFacing.values()) {

            putBandQuads(buffer, model.getQuads(state, side, MODEL_SEED), facing);
        }
        putBandQuads(buffer, model.getQuads(state, null, MODEL_SEED), facing);
        tessellator.draw();
        RainbowPigmeeShader.unbind();
    }

    private static void putBandQuads(BufferBuilder buffer, List<BakedQuad> quads, EnumFacing facing) {
        for (BakedQuad quad : quads) {
            int[] data = quad.getVertexData();
            int stride = data.length / 4;
            EnumFacing side = quad.getFace();
            for (int vertex = 0; vertex < 4; vertex++) {
                int offset = vertex * stride;
                putBandVertex(buffer, side, facing,
                        Float.intBitsToFloat(data[offset]),
                        Float.intBitsToFloat(data[offset + 1]),
                        Float.intBitsToFloat(data[offset + 2]));
            }
        }
    }

    /** 逐字对齐源码 RainbowPigmeeSurfaceLayer#vertex 的投影/深度/明暗计算 */
    private static void putBandVertex(BufferBuilder buffer, EnumFacing side, EnumFacing facing,
                                      float x, float y, float z) {
        // 在姿态变换前投影，转动或手持时像素保持贴附（源码注释）
        float u = side.getAxis() == EnumFacing.Axis.X ? z : x;
        float v = side.getAxis() == EnumFacing.Axis.Y ? z : y;
        // 方块四边形已包含朝向旋转，恢复前→后深度
        float depth;
        switch (facing) {
            case SOUTH: depth = 1.0F - z; break;
            case EAST: depth = 1.0F - x; break;
            case WEST: depth = x; break;
            default: depth = z; break;
        }
        float shade;
        switch (side) {
            case UP: shade = 1.0F; break;
            case DOWN: shade = 0.50F; break;
            case NORTH:
            case SOUTH: shade = 0.80F; break;
            default: shade = 0.60F; break;
        }
        buffer.pos(x + side.getXOffset() * SURFACE_OFFSET,
                        y + side.getYOffset() * SURFACE_OFFSET,
                        z + side.getZOffset() * SURFACE_OFFSET)
                .color(shade, shade, shade, 1.0F)
                .tex(u, v)
                // UV1 携带模型局部深度与高度，供 shader 做斜向色彩流动
                .tex(Math.round(depth * 4096.0F), Math.round(y * 4096.0F))
                .endVertex();
    }

    // ==================== 通道 2：面部标记 ====================

    public static void renderMarkings(Tessellator tessellator, IBakedModel model, @Nullable IBlockState state) {
        ensureSprites();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.enableAlpha();
        // 注意：baked quad 顶点数据是 ITEM 格式（8 int/顶点，含法线、无光照通道），
        // 缓冲格式必须与之一致，否则从第二个四边形起顶点流错位
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

        for (EnumFacing side : EnumFacing.values()) {

            putMarkingQuads(buffer, model.getQuads(state, side, MODEL_SEED));
        }
        putMarkingQuads(buffer, model.getQuads(state, null, MODEL_SEED));
        // ITEM 格式不带逐顶点光照，显式把光照贴图坐标设为全亮（对齐源码的自发光标记）
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        tessellator.draw();
        GlStateManager.disableAlpha();
    }

    private static void putMarkingQuads(BufferBuilder buffer, List<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
            EnumFacing face = quad.getFace();
            int[] data = quad.getVertexData().clone();
            int stride = data.length / 4;
            for (int vertex = 0; vertex < 4; vertex++) {
                int o = vertex * stride;
                // 沿法线微微抬升，避免与色带层深度冲突
                data[o] = Float.floatToRawIntBits(Float.intBitsToFloat(data[o]) + face.getXOffset() * MARKING_OFFSET);
                data[o + 1] = Float.floatToRawIntBits(Float.intBitsToFloat(data[o + 1]) + face.getYOffset() * MARKING_OFFSET);
                data[o + 2] = Float.floatToRawIntBits(Float.intBitsToFloat(data[o + 2]) + face.getZOffset() * MARKING_OFFSET);
                // UV 从基础贴图精灵重映射到标记贴图精灵（同尺寸同布局）
                float u = Float.intBitsToFloat(data[o + 4]);
                float v = Float.intBitsToFloat(data[o + 5]);
                data[o + 4] = Float.floatToRawIntBits(markingSprite.getInterpolatedU(baseSprite.getUnInterpolatedU(u)));
                data[o + 5] = Float.floatToRawIntBits(markingSprite.getInterpolatedV(baseSprite.getUnInterpolatedV(v)));
                // ITEM 格式的 int6 是法线+填充，保持原样（TESR/TEISR 已禁用标准光照，等同全亮）
            }
            buffer.addVertexData(data);
        }
    }

    /** shader 不可用时的降级：整体变色 tint 的贴图本体 + 标记层 */
    public static void renderFallback(Tessellator tessellator, IBakedModel model, @Nullable IBlockState state, int tint) {
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

        for (EnumFacing side : EnumFacing.values()) {

            putTintedQuads(buffer, model.getQuads(state, side, MODEL_SEED), tint);
        }
        putTintedQuads(buffer, model.getQuads(state, null, MODEL_SEED), tint);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        tessellator.draw();
    }

    private static void putTintedQuads(BufferBuilder buffer, List<BakedQuad> quads, int tint) {
        for (BakedQuad quad : quads) {
            int[] data = quad.getVertexData().clone();
            int stride = data.length / 4;
            for (int vertex = 0; vertex < 4; vertex++) {
                int o = vertex * stride;
                if (quad.hasTintIndex()) {
                    // 顶点颜色为 ABGR 整型，tint(0xRRGGBB) 需交换 R/B 通道
                    data[o + 3] = 0xFF000000 | (tint & 0xFF) << 16 | (tint & 0xFF00) | (tint >> 16 & 0xFF);
                }
            }
            buffer.addVertexData(data);
        }
    }

    private static void ensureSprites() {
        if (baseSprite != null) {
            return;
        }
        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        baseSprite = map.getAtlasSprite(Tags.MOD_ID + ":block/pigmee_fumo");
        markingSprite = map.getAtlasSprite(Tags.MOD_ID + ":block/rainbow_pigmee_fumo_overlay");
    }
}
