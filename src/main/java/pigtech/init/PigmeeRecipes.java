package pigtech.init;

import pigtech.Tags;
import pigtech.recipe.RecipeRainbowPigmeeCycle;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PigmeeRecipes {

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        RecipeRainbowPigmeeCycle cycle = new RecipeRainbowPigmeeCycle();
        cycle.setRegistryName(new ResourceLocation(Tags.MOD_ID, "rainbow_pigmee_color_cycle"));
        event.getRegistry().register(cycle);
    }
}
