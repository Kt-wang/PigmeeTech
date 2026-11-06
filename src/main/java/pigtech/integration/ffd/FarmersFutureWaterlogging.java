package pigtech.integration.ffd;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import pigtech.integration.PigmeeWaterlogging;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;

/** Loaded only when Farmer's Future Delight is installed. */
public final class FarmersFutureWaterlogging implements PigmeeWaterlogging.FluidHooks {
    public static void initialize() {
        WaterloggedBlockApi.registerAdapter(new WaterloggedBlockApi.Adapter() {
            @Override public boolean matches(IBlockState state) {
                return state.getBlock().getRegistryName() != null
                        && state.getBlock().getRegistryName().getNamespace().equals("pigtech")
                        && state.getPropertyKeys().contains(PigmeeWaterlogging.WATERLOGGED);
            }
            @Override public boolean isWaterlogged(IBlockState state) { return matches(state) && PigmeeWaterlogging.wet(state); }
            @Override public IBlockState withWaterlogged(IBlockState state, boolean wet) {
                return matches(state) ? state.withProperty(PigmeeWaterlogging.WATERLOGGED, wet) : state;
            }
        });
        for (var block : ForgeRegistries.BLOCKS.getValuesCollection())
            if (block.getRegistryName().getNamespace().equals("pigtech")
                    && block.getDefaultState().getPropertyKeys().contains(PigmeeWaterlogging.WATERLOGGED))
                WaterloggedBlockApi.registerRenderedBlock(block);
        PigmeeWaterlogging.hooks = new FarmersFutureWaterlogging();
    }
    @Override public void added(World world, BlockPos pos, IBlockState state) { WaterloggedBlockApi.onBlockAdded(world, pos, state.getBlock()); }
    @Override public void neighbor(World world, BlockPos pos, IBlockState state) { WaterloggedBlockApi.onNeighborChanged(world, pos, state.getBlock()); }
    @Override public void tick(World world, BlockPos pos, IBlockState state) { WaterloggedBlockApi.updateTick(world, pos, state); }
}
