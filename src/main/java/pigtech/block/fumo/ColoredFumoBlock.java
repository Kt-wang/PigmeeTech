/*
 * Pigmee Tech - ColoredFumoBlock
 * 16 色猪咪方块：与普通猪咪共用朝向/形状/TESR，颜色存于 TileFumo（0-15），
 * 物品侧用 metadata 记录颜色（方块 meta 仍表示朝向）。
 */
package pigtech.block.fumo;

import pigtech.tile.fumo.TileFumo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ColoredFumoBlock extends FumoBlock {

    public static final int COLOR_COUNT = 16;

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
                                ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileFumo) {
                int meta = stack.getMetadata();
                ((TileFumo) tile).setColor(meta >= 0 && meta < COLOR_COUNT ? meta : -1);
            }
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
                         int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        int color = colorAt(world, pos);
        Item self = Item.getItemFromBlock(this);
        for (ItemStack drop : drops) {
            if (drop.getItem() == self) {
                drop.setItemDamage(color);
            }
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
                                  EntityPlayer player) {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);
        if (stack.getItem() == Item.getItemFromBlock(this)) {
            stack.setItemDamage(colorAt(world, pos));
        }
        return stack;
    }

    private static int colorAt(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileFumo) {
            return Math.max(0, ((TileFumo) tile).getColor());
        }
        return 0;
    }
}
