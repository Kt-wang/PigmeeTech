/*
 * AE2 Crystal Science - ConnectionPredicates (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.client.ctm.ConnectionPredicates（裁剪到猪咪建材用到的规则）。
 */
package pigtech.client.ctm;

import pigtech.block.building.PigmeeBuildingPanelBlock;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** 猪咪建材用到的连接规则注册表 */
public final class ConnectionPredicates {

    /** 通用：任何相邻同类型方块都连接（对齐源码 same_block） */
    public static final ConnectionPredicate SAME_BLOCK = new ConnectionPredicate() {
        @Override
        public boolean isActive(IBlockAccess world, BlockPos pos, IBlockState self) {
            return true;
        }

        @Override
        public boolean connects(IBlockAccess world, BlockPos pos, IBlockState self, EnumFacing dir) {
            return connects(world, pos, self, pos.offset(dir));
        }

        @Override
        public boolean connects(IBlockAccess world, BlockPos pos, IBlockState self, BlockPos neighbourPos) {
            return world.getBlockState(neighbourPos).getBlock() == self.getBlock();
        }
    };

    /**
     * 猪咪板材专用：同色素板/框板互连（对齐指南"素板与框板使用同一种颜色时自动连纹"）。
     * 注意这比源码模型 JSON 里的 same_block 更宽——源码的指南文本描述的是同色互连。
     */
    public static final ConnectionPredicate PIGMEE_SAME_COLOR = new ConnectionPredicate() {
        @Override
        public boolean isActive(IBlockAccess world, BlockPos pos, IBlockState self) {
            return true;
        }

        @Override
        public boolean connects(IBlockAccess world, BlockPos pos, IBlockState self, EnumFacing dir) {
            return connects(world, pos, self, pos.offset(dir));
        }

        @Override
        public boolean connects(IBlockAccess world, BlockPos pos, IBlockState self, BlockPos neighbourPos) {
            if (!(self.getBlock() instanceof PigmeeBuildingPanelBlock)) {
                return false;
            }
            IBlockState neighbour = world.getBlockState(neighbourPos);
            return neighbour.getBlock() instanceof PigmeeBuildingPanelBlock
                    && ((PigmeeBuildingPanelBlock) neighbour.getBlock()).getColor()
                            == ((PigmeeBuildingPanelBlock) self.getBlock()).getColor();
        }
    };

    /**
     * 台阶（对齐源码 same_slab）：半砖只与同平面、同半值、同方块的邻居连接；
     * 双砖按同方块处理。
     */
    public static final ConnectionPredicate SAME_SLAB = new ConnectionPredicate() {
        @Override
        public boolean isActive(IBlockAccess world, BlockPos pos, IBlockState self) {
            return true;
        }

        @Override
        public boolean connects(IBlockAccess world, BlockPos pos, IBlockState self, EnumFacing dir) {
            return connects(world, pos, self, pos.offset(dir));
        }

        @Override
        public boolean connects(IBlockAccess world, BlockPos pos, IBlockState self, BlockPos neighbourPos) {
            if (!(self.getBlock() instanceof BlockSlab)) {
                return false;
            }
            BlockSlab slab = (BlockSlab) self.getBlock();
            // 半砖不与相隔整格的邻居连接（上/下表面保持边框）
            if (!slab.isDouble() && neighbourPos.getY() != pos.getY()) {
                return false;
            }
            IBlockState neighbour = world.getBlockState(neighbourPos);
            if (neighbour.getBlock() != self.getBlock()) {
                return false;
            }
            return slab.isDouble() || neighbour.getValue(BlockSlab.HALF) == self.getValue(BlockSlab.HALF);
        }
    };

    private ConnectionPredicates() {
    }
}
