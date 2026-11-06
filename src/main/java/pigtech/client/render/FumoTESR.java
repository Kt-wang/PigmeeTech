/*
 * Pigmee Tech - FumoTESR (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.client.FumoBlockRenderer.
 *
 * 世界内渲染：从 BlockRendererDispatcher 取方块 baked model 手工 tessellate。
 * 旋转时先把朝向重置为 NORTH，再绕方块中心 Y 轴旋转（与源码一致）。
 */
package pigtech.client.render;

import pigtech.block.fumo.FumoBlock;
import pigtech.tile.fumo.TileFumo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class FumoTESR extends TileEntitySpecialRenderer<TileFumo> {

    @Override
    public void render(TileFumo te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        World world = te.getWorld();
        if (world == null) {
            return;
        }
        IBlockState state = world.getBlockState(te.getPos());
        if (!(state.getBlock() instanceof FumoBlock)) {
            return;
        }

        boolean spinning = te.isSpinning();
        // 旋转时朝向归一为 NORTH，由 GL 旋转接管（对齐源码）
        IBlockState renderState = spinning
                ? state.withProperty(FumoBlock.FACING, EnumFacing.NORTH)
                : state;

        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBakedModel model = dispatcher.getModelForState(renderState);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        if (spinning) {
            GlStateManager.rotate(te.getRenderYRot(partialTicks), 0.0F, 1.0F, 0.0F);
        }
        GlStateManager.translate(-0.5D, 0.0D, -0.5D);

        boolean rainbow = state.getBlock() == pigtech.init.PigmeeBlocks.RAINBOW_PIGMEE_FUMO;
        if (rainbow && RainbowPigmeeShader.isAvailable()) {
            // 彩虹猪咪：shader 色带层取代贴图本体，再叠全亮面部标记（对齐源码渲染分层）
            float animationTicks = (float) (world.getTotalWorldTime() % 24000L + partialTicks);
            RainbowPigmeeSurface.renderBands(tessellator, model, renderState,
                    renderState.getValue(FumoBlock.FACING), animationTicks);
            RainbowPigmeeSurface.renderMarkings(tessellator, model, renderState);
        } else {
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            buffer.setTranslation(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
            dispatcher.getBlockModelRenderer().renderModel(world, model, renderState, te.getPos(), buffer, false);
            buffer.setTranslation(0, 0, 0);
            tessellator.draw();
        }

        GlStateManager.popMatrix();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        RenderHelper.enableStandardItemLighting();
    }
}
