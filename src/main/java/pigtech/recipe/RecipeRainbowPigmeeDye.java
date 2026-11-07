package pigtech.recipe;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

/** A fixed dye layout can also recolor four identical items, preserving their NBT. */
public final class RecipeRainbowPigmeeDye extends ShapedRecipes {
    public static final class Factory implements net.minecraftforge.common.crafting.IRecipeFactory {
        @Override public net.minecraft.item.crafting.IRecipe parse(net.minecraftforge.common.crafting.JsonContext context,
                                                                  com.google.gson.JsonObject json) {
            return new RecipeRainbowPigmeeDye(ShapedRecipes.deserialize(json));
        }
    }
    public RecipeRainbowPigmeeDye(ShapedRecipes base) {
        super(base.getGroup(), base.getWidth(), base.getHeight(), base.getIngredients(), base.getRecipeOutput());
    }
    private boolean matchesBases(InventoryCrafting inv) {
        if (inv.getWidth() != 3 || inv.getHeight() != 3 || getWidth() != 3 || getHeight() != 3) return false;
        for (int slot = 0; slot < 9; slot++)
            if (!getIngredients().get(slot).apply(inv.getStackInSlot(slot))) return false;
        return true;
    }
    private ItemStack recolor(InventoryCrafting inv) {
        if (inv.getWidth() != 3 || inv.getHeight() != 3 || getWidth() != 3 || getHeight() != 3
                || !RecipeRainbowPigmeeCycle.isRainbowPigmee(inv.getStackInSlot(4))
                || getRecipeOutput().getItem() != Items.DYE) return ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;
        int count = 0;
        for (int slot = 0; slot < 9; slot++) {
            if (slot == 4) continue;
            ItemStack stack = inv.getStackInSlot(slot);
            if (getIngredients().get(slot) == Ingredient.EMPTY) {
                if (!stack.isEmpty()) return ItemStack.EMPTY;
            } else {
                if (stack.isEmpty()) return ItemStack.EMPTY;
                if (target.isEmpty()) target = stack;
                else if (!ItemStack.areItemsEqual(target, stack) || !ItemStack.areItemStackTagsEqual(target, stack))
                    return ItemStack.EMPTY;
                count++;
            }
        }
        return count == 4 ? RainbowColoring.color(target,
                EnumDyeColor.byDyeDamage(getRecipeOutput().getMetadata()), count) : ItemStack.EMPTY;
    }
    @Override public boolean matches(InventoryCrafting inv, World world) {
        return matchesBases(inv) || !recolor(inv).isEmpty();
    }
    @Override public ItemStack getCraftingResult(InventoryCrafting inv) {
        return matchesBases(inv) ? super.getCraftingResult(inv) : recolor(inv);
    }
    @Override public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> result = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (RecipeRainbowPigmeeCycle.isRainbowPigmee(stack)) {
                ItemStack copy = stack.copy(); copy.setCount(1); result.set(slot, copy);
            }
        }
        return result;
    }
}
