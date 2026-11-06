/*
 * AE2 Crystal Science - RainbowPigmeeTEISR (1.12.2)
 * Port of ae2lt 1.21.1 client/render/HyperdimensionalPigmeeItemRenderer（彩虹分支）。
 *
 * 彩虹猪咪的物品形态渲染：与方块形态共用 RainbowPigmeeSurface 的两个通道
 * （shader 色带层 + 全亮面部标记层），深度参考朝向固定 NORTH（对齐源码 renderItem）。
 * 相机变换（GUI/手持/掉落物/头戴）由 ItemRenderer 在调用前应用。
 * shader 不可用时降级为整体变色 tint 的本体渲染。
 */
package pigtech.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

public class RainbowPigmeeTEISR extends TileEntityItemStackRenderer {

    @Override
    public void renderByItem(ItemStack stack) {
        Minecraft mc = Minecraft.getMinecraft();
        // 彩虹猪咪不再包 SpinningFumoBakedModel（TEISR 已接管），取到的即基础模型
        IBakedModel model = mc.getRenderItem().getItemModelMesher().getItemModel(stack);

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        // 保存调用方（背包/JEI/世界内）的 GL 状态，渲染后原样恢复。
        // 直接 disableBlend 曾导致背包后续空槽位背景图标失去混合而变黑。
        boolean lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean alphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        int blendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        int blendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        if (RainbowPigmeeShader.isAvailable()) {
            RainbowPigmeeSurface.renderBands(tessellator, model, null, EnumFacing.NORTH,
                    (float) RainbowPigmeeColors.animationTicks());
            RainbowPigmeeSurface.renderMarkings(tessellator, model, null);
        } else {
            RainbowPigmeeSurface.renderFallback(tessellator, model, null, RainbowPigmeeColors.currentColor());
            RainbowPigmeeSurface.renderMarkings(tessellator, model, null);
        }

        GlStateManager.popMatrix();
        // 逐项恢复调用方状态
        if (cullEnabled) {
            GlStateManager.enableCull();
        } else {
            GlStateManager.disableCull();
        }
        if (blendEnabled) {
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
        }
        GlStateManager.blendFunc(blendSrc, blendDst);
        if (alphaEnabled) {
            GlStateManager.enableAlpha();
        } else {
            GlStateManager.disableAlpha();
        }
        if (lightingEnabled) {
            RenderHelper.enableStandardItemLighting();
        }
    }
}
