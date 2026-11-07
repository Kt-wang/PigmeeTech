package pigtech;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
        if (net.minecraftforge.fml.common.Loader.isModLoaded("farmers_future_delight"))
            pigtech.integration.ffd.FarmersFutureWaterlogging.initialize();
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
