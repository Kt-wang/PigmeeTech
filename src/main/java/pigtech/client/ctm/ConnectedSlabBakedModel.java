/*
 * AE2 Crystal Science - ConnectedSlabBakedModel (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.client.ctm.ConnectedSlabBakedModel.
 *
 * 同一套 CTM 表面映射到台阶上；内凹面（半砖的切平面）放在无 cullface 的
 * quad 组里（side == null 时返回），侧面/底面逐顶点压扁到半高。
 */
package pigtech.client.ctm;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class ConnectedSlabBakedModel extends ConnectedTextureBakedModel {

    public enum SlabKind {
        BOTTOM, TOP, DOUBLE
    }

    private final SlabKind kind;

    public ConnectedSlabBakedModel(TextureAtlasSprite base, TextureAtlasSprite ctm, SlabKind kind) {
        super(base, ctm, ConnectionPredicates.SAME_SLAB);
        this.kind = kind;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (kind == SlabKind.DOUBLE) {
            return super.getQuads(state, side, rand);
        }
        EnumFacing insetFace = kind == SlabKind.BOTTOM ? EnumFacing.UP : EnumFacing.DOWN;
        // 整格外的邻居无法遮挡 y=0.5 处的切面
        if (side == insetFace) {
            return java.util.Collections.emptyList();
        }
        List<BakedQuad> quads = super.getQuads(state, side == null ? insetFace : side, rand);
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertexData().clone();
            int stride = vertices.length / 4;
            for (int v = 0; v < 4; v++) {
                float y = Float.intBitsToFloat(vertices[v * stride + 1]);
                vertices[v * stride + 1] = Float.floatToRawIntBits(
                        y * 0.5F + (kind == SlabKind.TOP ? 0.5F : 0));
            }
            result.add(new BakedQuad(vertices, quad.getTintIndex(), quad.getFace(),
                    quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat()));
        }
        return result;
    }
}
