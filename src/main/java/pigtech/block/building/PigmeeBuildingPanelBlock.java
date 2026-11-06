/*
 * Pigmee Tech - PigmeeBuildingPanelBlock (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.block.PigmeeBuildingPanelBlock.
 *
 * 16 色素板/框板：纯装饰方块。CTM 连纹由 pigtech.client.ctm 提供，
 * 本类负责在网格构建时算出连接数据并挂到扩展状态上。
 */
package pigtech.block.building;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import pigtech.client.ctm.ConnectionPredicates;
import pigtech.client.ctm.CtmConnectionState;
import pigtech.client.ctm.PigmeeCtm;

public class PigmeeBuildingPanelBlock extends Block {

    private final EnumDyeColor color;

    public PigmeeBuildingPanelBlock(EnumDyeColor color) {
        super(Material.ROCK);
        this.setSoundType(SoundType.STONE);
        this.setHardness(1.5F);
        this.setResistance(6.0F);
        this.setHarvestLevel("pickaxe", 0);
        this.color = color;
    }

    public EnumDyeColor getColor() {
        return color;
    }

    // ==================== CTM（连纹） ====================

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new IProperty[0],
                new IUnlistedProperty[]{PigmeeCtm.CONNECTION});
    }

    /** 网格构建时（仅客户端）计算连接数据，随扩展状态传给烘焙模型 */
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state instanceof IExtendedBlockState) {
            CtmConnectionState conn = CtmConnectionState.compute(
                    world, pos, state, ConnectionPredicates.PIGMEE_SAME_COLOR);
            if (conn != null) {
                return ((IExtendedBlockState) state)
                        .withProperty(PigmeeCtm.CONNECTION, conn);
            }
        }
        return state;
    }
}
