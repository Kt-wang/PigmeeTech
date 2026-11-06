package pigtech.init;

import pigtech.PigmeeTech;
import pigtech.Tags;
import pigtech.tile.fumo.TileFumo;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class PigmeeBlockEntities {

    public static void register() {
        GameRegistry.registerTileEntity(TileFumo.class, Tags.MOD_ID + ":fumo");
        PigmeeTech.LOGGER.info("Registered pigmee tile entities");
    }
}
