/*
 * AE2 Crystal Science - FumoBlock (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.block.FumoBlock.
 *
 * 猪咪玩偶方块：可朝向、羊毛手感、右键切换旋转（由 TileFumo + FumoTESR 驱动）。
 * 1.12.2 没有含水方块机制，源码的 WATERLOGGED 属性无法移植，其余行为全部保留。
 */
package pigtech.block.fumo;

import pigtech.tile.fumo.TileFumo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FumoBlock extends Block {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    private static final AxisAlignedBB SHAPE_NORTH = new AxisAlignedBB(3.9 / 16.0, 0, 4.0 / 16.0, 12.2 / 16.0, 13.6 / 16.0, 14.7 / 16.0);
    private static final AxisAlignedBB SHAPE_SOUTH = new AxisAlignedBB(3.8 / 16.0, 0, 1.3 / 16.0, 12.1 / 16.0, 13.6 / 16.0, 12.0 / 16.0);
    private static final AxisAlignedBB SHAPE_WEST = new AxisAlignedBB(4.0 / 16.0, 0, 3.8 / 16.0, 14.7 / 16.0, 13.6 / 16.0, 12.2 / 16.0);
    private static final AxisAlignedBB SHAPE_EAST = new AxisAlignedBB(1.3 / 16.0, 0, 3.8 / 16.0, 12.0 / 16.0, 13.6 / 16.0, 12.2 / 16.0);

    public FumoBlock() {
        super(Material.CLOTH);
        this.setSoundType(net.minecraft.block.SoundType.CLOTH);
        this.setHardness(0.5F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    /** 对齐源码 getLightBlock = 2 */
    @Override
    public int getLightOpacity(IBlockState state) {
        return 2;
    }

    /** 对齐源码 getShadeBrightness = 1.0（全亮阴影） */
    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return getShapeForFacing(state.getValue(FACING));
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return getShapeForFacing(blockState.getValue(FACING));
    }

    private static AxisAlignedBB getShapeForFacing(EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                return SHAPE_SOUTH;
            case WEST:
                return SHAPE_WEST;
            case EAST:
                return SHAPE_EAST;
            default:
                return SHAPE_NORTH;
        }
    }

    /** ENTITYBLOCK_ANIMATED：普通管线不渲染，交给 FumoTESR */
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public net.minecraft.util.BlockRenderLayer getRenderLayer() {
        return net.minecraft.util.BlockRenderLayer.CUTOUT;
    }

    /** 右键：切换旋转状态（服务端为准，通过 TE 数据包同步客户端） */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileFumo) {
                ((TileFumo) te).toggleSpinning();
            }
        }
        return true;
    }

    /** 命名过的猪咪被破坏/中键选取时保留名字（jeb_ 彩蛋不因此丢失） */
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileFumo) {
            String name = ((TileFumo) te).getCustomName();
            if (name != null) {
                for (ItemStack drop : drops) {
                    drop.setStackDisplayName(name);
                }
            }
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileFumo) {
            String name = ((TileFumo) te).getCustomName();
            if (name != null) {
                stack.setStackDisplayName(name);
            }
        }
        return stack;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileFumo();
    }
}
