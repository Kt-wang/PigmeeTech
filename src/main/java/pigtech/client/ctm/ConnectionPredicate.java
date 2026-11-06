/*
 * Pigmee Tech - ConnectionPredicate (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.client.ctm.ConnectionPredicate.
 */
package pigtech.client.ctm;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * 决定连纹方块的 CTM 外观是否生效，以及某个邻居方向是否"连接"（消除边框）。
 * 可插拔，不同方块可提供各自规则（对齐源码）。
 */
public interface ConnectionPredicate {

    /** 返回 false 时整块用普通 base 贴图渲染（不做 CTM） */
    boolean isActive(IBlockAccess world, BlockPos pos, IBlockState self);

    /** 指定方向上的邻居是否与本方块连接 */
    boolean connects(IBlockAccess world, BlockPos pos, IBlockState self, EnumFacing dir);

    /** 任意位置（含对角）的邻居是否与本方块连接 */
    default boolean connects(IBlockAccess world, BlockPos pos, IBlockState self, BlockPos neighbourPos) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (pos.offset(dir).equals(neighbourPos)) {
                return connects(world, pos, self, dir);
            }
        }
        return false;
    }
}
