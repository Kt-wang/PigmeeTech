/*
 * Pigmee Tech - RecipeRainbowPigmeeCycle (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.recipe.RainbowPigmeeColorCycleRecipe.
 *
 * 一只彩虹猪咪 + 一件可染色物品 → 该物品的下一色（16 色循环）。
 * 猪咪永不消耗（getRemainingItems 原样返还），支持背包 2×2 合成栏。
 */
package pigtech.recipe;

import pigtech.init.PigmeeBlocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public class RecipeRainbowPigmeeCycle extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
        return !RainbowColoring.next(findTarget(inv)).isEmpty();
    }

    @Override
    @Nonnull
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        return RainbowColoring.next(findTarget(inv));
    }

    /** 彩虹猪咪留在原位；目标物品由原版合成逻辑正常消耗 1 件 */
    @Override
    @Nonnull
    public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (isRainbowPigmee(stack)) {
                ItemStack kept = stack.copy();
                kept.setCount(1);
                remaining.set(slot, kept);
            }
        }
        return remaining;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    @Nonnull
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    static boolean isRainbowPigmee(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(PigmeeBlocks.RAINBOW_PIGMEE_FUMO);
    }

    /** 恰好一只彩虹猪咪 + 恰好一件其他物品；不满足则返回 EMPTY（对齐源码 findTarget） */
    private static ItemStack findTarget(InventoryCrafting inv) {
        ItemStack target = ItemStack.EMPTY;
        boolean foundPigmee = false;
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (isRainbowPigmee(stack)) {
                if (foundPigmee) {
                    return ItemStack.EMPTY;
                }
                foundPigmee = true;
            } else if (target.isEmpty()) {
                target = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        return foundPigmee ? target : ItemStack.EMPTY;
    }
}
