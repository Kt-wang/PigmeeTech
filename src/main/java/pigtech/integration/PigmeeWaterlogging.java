package pigtech.integration;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Saved native state; the optional fluid API is isolated in the FFD integration class. */
public final class PigmeeWaterlogging {
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    public interface FluidHooks {
        void added(World world, BlockPos pos, IBlockState state);
        void neighbor(World world, BlockPos pos, IBlockState state);
        void tick(World world, BlockPos pos, IBlockState state);
    }
    public static FluidHooks hooks;
    private PigmeeWaterlogging() { }
    public static boolean sourceAt(World world, BlockPos pos) {
        if (hooks == null) return false;
        IBlockState state = world.getBlockState(pos);
        return state.getMaterial() == Material.WATER && state.getBlock() instanceof BlockLiquid
                && state.getValue(BlockLiquid.LEVEL) == 0;
    }
    public static boolean wet(IBlockState state) {
        return state.getPropertyKeys().contains(WATERLOGGED) && state.getValue(WATERLOGGED);
    }
    public static void restore(World world, BlockPos pos, IBlockState old) {
        if (!world.isRemote && wet(old) && world.isAirBlock(pos))
            world.setBlockState(pos, net.minecraft.init.Blocks.FLOWING_WATER.getDefaultState(), 3);
    }
}
