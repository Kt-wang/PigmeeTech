/*
 * Pigmee Tech - ConnectedTextureBakedModel (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.client.ctm.ConnectedTextureBakedModel.
 *
 * 通用连纹烘焙模型（Mekanism 风格 compact CTM）。
 * 1.12.2 适配：连接数据由方块 getExtendedState 计算并经 IUnlistedProperty
 * 随 IExtendedBlockState 传入（对应源码的 ModelData 通道）；无数据（物品渲染）
 * 时回退为整面 base 贴图。
 */
package pigtech.client.ctm;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectedTextureBakedModel implements IBakedModel {

    protected final TextureAtlasSprite baseSprite;
    protected final TextureAtlasSprite ctmSprite;

    public ConnectedTextureBakedModel(TextureAtlasSprite baseSprite, TextureAtlasSprite ctmSprite) {
        this.baseSprite = baseSprite;
        this.ctmSprite = ctmSprite;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side == null) {
            return Collections.emptyList();
        }
        CtmConnectionState conn = null;
        if (state instanceof IExtendedBlockState) {
            conn = ((IExtendedBlockState) state).getValue(PigmeeCtm.CONNECTION);
        }
        if (conn == null) {
            // 未激活或无世界数据（物品渲染）→ 普通整面贴图
            return Collections.singletonList(CtmFaceGeometry.fullFace(side, baseSprite));
        }
        if (conn.culled(side)) {
            return Collections.emptyList();
        }
        int edges = conn.edges(side);
        int corners = conn.corners(side);
        List<BakedQuad> quads = new ArrayList<>(4);
        for (int sq = 0; sq < 2; sq++) {
            for (int tq = 0; tq < 2; tq++) {
                CtmTileSelector.Tile tile = CtmTileSelector.select(
                        CtmTileSelector.quadrant(sq, tq), edges, corners);
                TextureAtlasSprite sprite =
                        tile.source == CtmTileSelector.Source.BASE ? baseSprite : ctmSprite;
                quads.add(CtmFaceGeometry.quadrant(side, sq, tq, tile, sprite));
            }
        }
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return baseSprite;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
