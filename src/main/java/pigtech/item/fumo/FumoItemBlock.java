/*
 * AE2 Crystal Science - FumoItemBlock (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.item.FumoBlockItem.
 *
 * 猪咪玩偶物品：4 行 tooltip（前 3 灰、末行淡紫），可戴在头上。
 * 1.12.2 没有 Equipable 接口，通过 isValidArmor(HEAD) 允许放入头盔槽，
 * 并支持右键空手直接戴上（对齐 ItemArmor 的行为）。
 */
package pigtech.item.fumo;

import pigtech.tile.fumo.TileFumo;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class FumoItemBlock extends ItemBlock {

    private final String tooltipKey;
    private final EnumRarity rarity;

    public FumoItemBlock(Block block, String tooltipKey) {
        this(block, tooltipKey, EnumRarity.COMMON);
    }

    public FumoItemBlock(Block block, String tooltipKey, EnumRarity rarity) {
        super(block);
        this.tooltipKey = tooltipKey;
        this.rarity = rarity;
    }

    /** 允许放进头盔槽（SlotArmor 会询问此方法） */
    @Override
    public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
        return armorType == EntityEquipmentSlot.HEAD;
    }

    /**
     * 彩虹猪咪作为合成催化剂永不消耗（对齐源码 PigmeeBuildingRecipe 的保留语义）。
     * 1.12.2 用原版容器物品机制实现——这样配方可以用普通 JSON 编写，JEI 才能显示
     * （JEI 4.x 只包装 Shaped/Shapeless 四种原版配方类，自定义 IRecipe 类会被静默跳过）。
     * 循环换色配方（RecipeRainbowPigmeeCycle）有自己的 getRemainingItems 覆写，不受影响。
     */
    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return block == pigtech.init.PigmeeBlocks.RAINBOW_PIGMEE_FUMO;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        ItemStack kept = stack.copy();
        kept.setCount(1);
        return kept;
    }

    /** 右键空手：直接戴到头上（对齐 ItemArmor#onItemRightClick） */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (playerIn.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
            playerIn.setItemStackToSlot(EntityEquipmentSlot.HEAD, stack.splitStack(1));
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    /** 放置时把物品显示名（如 jeb_）写入方块实体，雷击彩蛋依赖它 */
    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        boolean placed = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (placed && stack.hasDisplayName()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileFumo) {
                ((TileFumo) te).setCustomName(stack.getDisplayName());
            }
        }
        return placed;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return rarity;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (tooltipKey == null) {
            return;
        }
        tooltip.add(TextFormatting.GRAY + I18n.format(tooltipKey + ".1"));
        tooltip.add(TextFormatting.GRAY + I18n.format(tooltipKey + ".2"));
        tooltip.add(TextFormatting.GRAY + I18n.format(tooltipKey + ".3"));
        tooltip.add(TextFormatting.LIGHT_PURPLE + I18n.format(tooltipKey + ".4"));
    }
}
