/*
 * Pigmee Tech - PigmeeTab (1.12.2)
 * 猪咪科技专属创造物品栏，图标为猪咪玩偶。
 */
package pigtech.init;

import pigtech.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PigmeeTab {

    public static final CreativeTabs INSTANCE = new CreativeTabs(Tags.MOD_ID + ".pigmee") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Item.getItemFromBlock(PigmeeBlocks.PIGMEE_FUMO));
        }
    };
}
