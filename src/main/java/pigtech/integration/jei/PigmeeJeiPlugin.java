/*
 * Pigmee Tech - minimal JEI plugin (rainbow pigmee info pages)
 */
package pigtech.integration.jei;

import pigtech.init.PigmeeBlocks;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

@JEIPlugin
public class PigmeeJeiPlugin implements IModPlugin {

    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.addIngredientInfo(
            new ItemStack(Item.getItemFromBlock(PigmeeBlocks.RAINBOW_PIGMEE_FUMO)),
            ItemStack.class,
            "jei.pigtech.rainbow_pigmee.info",
            "jei.pigtech.rainbow_pigmee.coloring");
    }
}
