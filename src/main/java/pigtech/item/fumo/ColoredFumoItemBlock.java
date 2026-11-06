/*
 * Pigmee Tech - ColoredFumoItemBlock
 * 16 色猪咪物品：metadata 0-15 对应 EnumDyeColor 顺序；名称按颜色本地化，
 * 可戴头/可放置（放置时把 metadata 写进 TileFumo）。
 */
package pigtech.item.fumo;

import pigtech.init.PigmeeBlocks;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;

public class ColoredFumoItemBlock extends FumoItemBlock {

    public ColoredFumoItemBlock(Block block) {
        super(block, "tooltip.pigtech.pigmee_fumo");
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        for (int meta = 0; meta < 16; meta++) {
            items.add(new ItemStack(this, 1, meta));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        int meta = MathHelper.clamp(stack.getMetadata(), 0, 15);
        String colorName = I18n.translateToLocal("pigtech.fumo.color." + PigmeeBlocks.PIGMEE_COLOR_NAMES[meta]);
        return I18n.translateToLocalFormatted("pigtech.fumo.colored_name", colorName);
    }
}
