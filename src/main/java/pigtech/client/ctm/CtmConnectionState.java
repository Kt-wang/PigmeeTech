/*
 * Pigmee Tech - CtmConnectionState (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.client.ctm.CtmConnectionState.
 *
 * 一个方块的预计算连接数据，通过未列入的方块状态属性（IUnlistedProperty）
 * 随 IExtendedBlockState 传给烘焙模型。edgeConnect 按面（EnumFacing#getIndex）
 * 存 4 位掩码（面内四条边，见 CtmFaceGeometry 边常量）；cornerConnect 存
 * 4 位对角掩码（CtmTileSelector.Quadrant#ordinal 顺序）；faceCulled 标记
 * 对面也连接的内部面（不渲染）。
 */
package pigtech.client.ctm;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public final class CtmConnectionState {

    private static final EnumFacing[] FACES = EnumFacing.VALUES;

    private final boolean[] faceCulled;
    private final int[] edgeConnect;
    private final int[] cornerConnect;

    public CtmConnectionState(boolean[] faceCulled, int[] edgeConnect, int[] cornerConnect) {
        this.faceCulled = faceCulled;
        this.edgeConnect = edgeConnect;
        this.cornerConnect = cornerConnect;
    }

    public boolean culled(EnumFacing face) {
        return faceCulled[face.getIndex()];
    }

    public int edges(EnumFacing face) {
        return edgeConnect[face.getIndex()];
    }

    public int corners(EnumFacing face) {
        return cornerConnect[face.getIndex()];
    }

    /** 谓词未激活时返回 null（模型降级为普通 base 贴图） */
    @Nullable
    public static CtmConnectionState compute(IBlockAccess world, BlockPos pos, IBlockState state,
                                             ConnectionPredicate predicate) {
        if (!predicate.isActive(world, pos, state)) {
            return null;
        }
        boolean[] culled = new boolean[FACES.length];
        int[] edges = new int[FACES.length];
        int[] corners = new int[FACES.length];
        for (EnumFacing face : FACES) {
            int idx = face.getIndex();
            culled[idx] = predicate.connects(world, pos, state, face);
            int mask = 0;
            for (int edge = 0; edge < 4; edge++) {
                if (predicate.connects(world, pos, state, CtmFaceGeometry.neighborDir(face, edge))) {
                    mask |= (1 << edge);
                }
            }
            edges[idx] = mask;
            int cornerMask = 0;
            for (CtmTileSelector.Quadrant quadrant : CtmTileSelector.Quadrant.values()) {
                if (predicate.connects(world, pos, state, CtmFaceGeometry.cornerPos(pos, face, quadrant))) {
                    cornerMask |= 1 << quadrant.ordinal();
                }
            }
            corners[idx] = cornerMask;
        }
        return new CtmConnectionState(culled, edges, corners);
    }
}
