package pigtech;

import pigtech.init.PigmeeBlockEntities;
import pigtech.init.PigmeeBlocks;
import pigtech.init.PigmeeItems;
import pigtech.init.PigmeeOreDict;
import pigtech.init.PigmeeRecipes;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Pigmee Tech（猪咪科技）—— 从 AE2 Crystal Science: Legacy 拆分出的独立模组。
 * 需要 AE2（染料基底配方使用 ae2:silicon）；包含猪咪玩偶、猪咪建材与彩虹猪咪换色。
 */
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:ae2;after:farmers_future_delight")
public class PigmeeTech {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.Instance(Tags.MOD_ID)
    public static PigmeeTech INSTANCE;

    @SidedProxy(clientSide = "pigtech.client.PigmeeClientProxy",
        serverSide = "pigtech.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(PigmeeBlocks.class);
        MinecraftForge.EVENT_BUS.register(PigmeeItems.class);
        MinecraftForge.EVENT_BUS.register(PigmeeRecipes.class);
        PigmeeBlockEntities.register();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PigmeeOreDict.register();
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
