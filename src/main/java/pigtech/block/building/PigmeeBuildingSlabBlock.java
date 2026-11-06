/*
 * AE2 Crystal Science - PigmeeBuildingSlabBlock (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.block.PigmeeBuildingSlabBlock.
 *
 * 猪咪建材台阶。1.12.2 的台阶必须拆成 半砖(Half) + 双砖(Double) 两个方块注册，
 * Double 无物品、不掉落自身（掉落物/中键选取都转给 Half）。
 * color 为 null 表示无色的基础建材台阶。
 */
package pigtech.block.building;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class PigmeeBuildingSlabBlock extends BlockSlab {

    /**
     * 半砖/双砖共有的哑变体属性。必须存在：ItemSlab 合并两个半砖时会调用
     * doubleSlab.getDefaultState().withProperty(getVariantProperty(), getTypeForItem(stack))，
     * 若双砖没有该属性会直接 IllegalArgumentException（原版石台阶用 variant=stone 同理）。
     */
    public static final net.minecraft.block.properties.PropertyEnum<SlabVariant> VARIANT =
            net.minecraft.block.properties.PropertyEnum.create("variant", SlabVariant.class);

    public enum SlabVariant implements net.minecraft.util.IStringSerializable {
        DEFAULT;

        @Override
        public String getName() {
            return "default";
        }
    }

    @Nullable
    private final net.minecraft.item.EnumDyeColor color;

    protected PigmeeBuildingSlabBlock(@Nullable net.minecraft.item.EnumDyeColor color) {
        super(Material.ROCK);
        this.setSoundType(net.minecraft.block.SoundType.STONE);
        this.setHardness(1.5F);
        this.setResistance(6.0F);
        this.setHarvestLevel("pickaxe", 0);
        this.color = color;
        this.useNeighborBrightness = true;

        IBlockState state = this.blockState.getBaseState();
        if (!this.isDouble()) {
            state = state.withProperty(HALF, EnumBlockHalf.BOTTOM)
                    .withProperty(pigtech.integration.PigmeeWaterlogging.WATERLOGGED, false);
        }
        this.setDefaultState(state);
    }

    @Nullable
    public net.minecraft.item.EnumDyeColor getColor() {
        return color;
    }

    @Override
    public String getTranslationKey(int meta) {
        return this.getTranslationKey();
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return SlabVariant.DEFAULT;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        // 扩展状态容器：listed(HALF+VARIANT 或仅 VARIANT) + unlisted(CTM 连接数据)
        return new net.minecraftforge.common.property.ExtendedBlockState(this,
                this.isDouble() ? new IProperty[]{VARIANT} : new IProperty[]{HALF, VARIANT, pigtech.integration.PigmeeWaterlogging.WATERLOGGED},
                new net.minecraftforge.common.property.IUnlistedProperty[]{pigtech.client.ctm.PigmeeCtm.CONNECTION});
    }

    /** 网格构建时（仅客户端）计算连接数据，随扩展状态传给烘焙模型 */
    @Override
    public IBlockState getExtendedState(IBlockState state, net.minecraft.world.IBlockAccess world,
                                        net.minecraft.util.math.BlockPos pos) {
        if (state instanceof net.minecraftforge.common.property.IExtendedBlockState) {
            pigtech.client.ctm.CtmConnectionState conn = pigtech.client.ctm.CtmConnectionState.compute(
                    world, pos, state, pigtech.client.ctm.ConnectionPredicates.SAME_SLAB);
            if (conn != null) {
                return ((net.minecraftforge.common.property.IExtendedBlockState) state)
                        .withProperty(pigtech.client.ctm.PigmeeCtm.CONNECTION, conn);
            }
        }
        return state;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = this.getDefaultState();
        if (!this.isDouble()) {
            state = state.withProperty(HALF, (meta & 8) != 0 ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM)
                    .withProperty(pigtech.integration.PigmeeWaterlogging.WATERLOGGED, (meta & 4) != 0);
        }
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (this.isDouble()) {
            return 0;
        }
        return (state.getValue(HALF) == EnumBlockHalf.TOP ? 8 : 0)
                | (pigtech.integration.PigmeeWaterlogging.wet(state) ? 4 : 0);
    }

    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, net.minecraft.util.EnumFacing facing,
            float hitX, float hitY, float hitZ, int meta, net.minecraft.entity.EntityLivingBase placer) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
        return isDouble() ? state : state.withProperty(pigtech.integration.PigmeeWaterlogging.WATERLOGGED,
                pigtech.integration.PigmeeWaterlogging.sourceAt(world, pos));
    }
    @Override public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (pigtech.integration.PigmeeWaterlogging.hooks != null) pigtech.integration.PigmeeWaterlogging.hooks.added(world, pos, state);
    }
    @Override public void neighborChanged(IBlockState state, World world, BlockPos pos, net.minecraft.block.Block block, BlockPos from) {
        super.neighborChanged(state, world, pos, block, from);
        if (pigtech.integration.PigmeeWaterlogging.hooks != null) pigtech.integration.PigmeeWaterlogging.hooks.neighbor(world, pos, state);
    }
    @Override public void updateTick(World world, BlockPos pos, IBlockState state, java.util.Random random) {
        if (pigtech.integration.PigmeeWaterlogging.hooks != null) pigtech.integration.PigmeeWaterlogging.hooks.tick(world, pos, state);
    }
    @Override public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        pigtech.integration.PigmeeWaterlogging.restore(world, pos, state);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    /** 半砖：正常注册物品（ItemSlab），参与合成与创造栏 */
    public static class Half extends PigmeeBuildingSlabBlock {
        public Half(@Nullable net.minecraft.item.EnumDyeColor color) {
            super(color);
        }

        @Override
        public boolean isDouble() {
            return false;
        }
    }

    /** 双砖：不注册物品，掉落与中键选取都转给对应的 Half */
    public static class Double extends PigmeeBuildingSlabBlock {
        private final Half half;

        public Double(@Nullable net.minecraft.item.EnumDyeColor color, Half half) {
            super(color);
            this.half = half;
        }

        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        public Item getItemDropped(IBlockState state, Random rand, int fortune) {
            return Item.getItemFromBlock(half);
        }

        @Override
        public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
            return new ItemStack(half);
        }
    }
}
