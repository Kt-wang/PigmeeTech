/*
 * Pigmee Tech - ore dictionary entries for the 16-color pigmee cycle
 */
package pigtech.init;

import pigtech.block.fumo.ColoredFumoBlock;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class PigmeeOreDict {

    private PigmeeOreDict() {
    }

    /** 与 EnumDyeColor metadata 同序的颜色名（用于 dyeXxx 矿辞） */
    private static final String[] ORE_COLOR_NAMES = {
        "White", "Orange", "Magenta", "LightBlue", "Yellow", "Lime", "Pink", "Gray",
        "LightGray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"};

    public static void register() {
        // 原版染料没有 dyeXxx 矿辞（Forge 只注册了通配的 "dye"），这里补齐，
        // 这样 16 色猪咪配方可用矿辞匹配，其他 mod 注册的同名染料也会自动兼容。
        for (int meta = 0; meta < 16; meta++) {
            ItemStack dye = new ItemStack(Items.DYE, 1, 15 - meta);
            OreDictionary.registerOre("dye" + ORE_COLOR_NAMES[meta], dye);
        }
        // 兼容常见别名
        OreDictionary.registerOre("dyeSilver", new ItemStack(Items.DYE, 1, 7));

        OreDictionary.registerOre("pigmeeFumo", new ItemStack(PigmeeBlocks.PIGMEE_FUMO));

        Item colored = Item.getItemFromBlock(PigmeeBlocks.PIGMEE_FUMO_COLORED);
        for (int meta = 0; meta < ColoredFumoBlock.COLOR_COUNT; meta++) {
            ItemStack stack = new ItemStack(colored, 1, meta);
            OreDictionary.registerOre("pigmeeFumo", stack);
            OreDictionary.registerOre("pigmeeFumoColored", stack);
        }
    }
}
