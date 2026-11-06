/*
 * Pigmee Tech - Item registration (dye base)
 */
package pigtech.init;

import pigtech.Tags;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PigmeeItems {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        // 染料基底：彩虹猪咪制染料配方的材料（ae2lt dye_base）
        reg(event, new Item(), "dye_base");
    }

    private static void reg(RegistryEvent.Register<Item> event, Item item, String name) {
        item.setRegistryName(Tags.MOD_ID, name);
        item.setTranslationKey(name);
        item.setCreativeTab(PigmeeTab.INSTANCE);
        event.getRegistry().register(item);
    }
}
