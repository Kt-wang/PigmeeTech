/*
 * AE2 Crystal Science - PigmeeBuildingPanelBlock (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.block.PigmeeBuildingPanelBlock.
 *
 * 16 色素板/框板：纯装饰方块。源码的 CTM 连纹由自定义 baked model 实现，
 * 1.12.2 首版不移植连纹（使用独立的整砖贴图）。
 */
package pigtech.block.building;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;

public class PigmeeBuildingPanelBlock extends Block {

    private final EnumDyeColor color;
    private final boolean framed;

    public PigmeeBuildingPanelBlock(EnumDyeColor color, boolean framed) {
        super(Material.ROCK);
        this.setSoundType(net.minecraft.block.SoundType.STONE);
        this.setHardness(1.5F);
        this.setResistance(6.0F);
        this.setHarvestLevel("pickaxe", 0);
        this.color = color;
        this.framed = framed;
    }

    public EnumDyeColor getColor() {
        return color;
    }

    public boolean isFramed() {
        return framed;
    }

    // ==================== CTM（连纹） ====================

    @Override
    protected net.minecraft.block.state.BlockStateContainer createBlockState() {
        return new net.minecraftforge.common.property.ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty[0],
                new net.minecraftforge.common.property.IUnlistedProperty[]{pigtech.client.ctm.PigmeeCtm.CONNECTION});
    }

    /** 网格构建时（仅客户端）计算连接数据，随扩展状态传给烘焙模型 */
    @Override
    public IBlockState getExtendedState(IBlockState state, net.minecraft.world.IBlockAccess world,
                                        net.minecraft.util.math.BlockPos pos) {
        if (state instanceof net.minecraftforge.common.property.IExtendedBlockState) {
            pigtech.client.ctm.CtmConnectionState conn = pigtech.client.ctm.CtmConnectionState.compute(
                    world, pos, state, pigtech.client.ctm.ConnectionPredicates.PIGMEE_SAME_COLOR);
            if (conn != null) {
                return ((net.minecraftforge.common.property.IExtendedBlockState) state)
                        .withProperty(pigtech.client.ctm.PigmeeCtm.CONNECTION, conn);
            }
        }
        return state;
    }
}
