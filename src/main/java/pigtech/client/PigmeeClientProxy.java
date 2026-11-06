/*
 * Pigmee Tech - client proxy
 * 负责：方块/物品模型注册、猪咪 TESR、彩虹猪咪 TEISR、旋转猪咪物品模型、彩虹面部贴图 stitch。
 * CTM（连纹板材/台阶）由 pigtech.client.ctm.PigmeeCtm 自己的 @Mod.EventBusSubscriber 处理。
 */
package pigtech.client;

import pigtech.CommonProxy;
import pigtech.Tags;
import pigtech.client.render.FumoTESR;
import pigtech.client.render.RainbowPigmeeTEISR;
import pigtech.client.render.model.SpinningFumoBakedModel;
import pigtech.init.PigmeeBlocks;
import pigtech.tile.fumo.TileFumo;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;

public class PigmeeClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // 猪咪玩偶：旋转动画渲染器
        ClientRegistry.bindTileEntitySpecialRenderer(TileFumo.class, new FumoTESR());
        // 彩虹猪咪：物品形态由 TEISR 渲染（shader 色带 + 面部标记，对齐源码）
        Item.getItemFromBlock(PigmeeBlocks.RAINBOW_PIGMEE_FUMO)
            .setTileEntityItemStackRenderer(new RainbowPigmeeTEISR());
    }

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event) {
        // 方块物品模型
        for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            ResourceLocation rl = block.getRegistryName();
            if (rl != null && rl.toString().startsWith(Tags.MOD_ID + ":")) {
                Item item = Item.getItemFromBlock(block);
                if (item == null || item == net.minecraft.init.Items.AIR) {
                    continue;
                }
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(rl, "inventory"));
            }
        }
        // 16 色猪咪：每个 metadata 都要注册同一个 inventory 模型
        Item coloredFumo = Item.getItemFromBlock(PigmeeBlocks.PIGMEE_FUMO_COLORED);
        ModelResourceLocation coloredMrl = new ModelResourceLocation(Tags.MOD_ID + ":pigmee_fumo_colored", "inventory");
        for (int meta = 0; meta < 16; meta++) {
            ModelLoader.setCustomModelResourceLocation(coloredFumo, meta, coloredMrl);
        }

        // 独立物品模型（dye_base）
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            ResourceLocation rl = item.getRegistryName();
            if (rl != null && rl.toString().startsWith(Tags.MOD_ID + ":")) {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(rl, "inventory"));
            }
        }
    }

    /** 彩虹猪咪面部标记贴图不被任何已加载模型引用，手动缝进方块贴图集 */
    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(new ResourceLocation(Tags.MOD_ID, "block/rainbow_pigmee_fumo_overlay"));
    }

    /** 猪咪玩偶物品模型：包一层 SpinningFumoBakedModel，戴在头上时持续旋转（对齐源码） */
    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        wrapFumoItemModel(event, "pigmee_fumo");
        wrapFumoItemModel(event, "creative_pigmee_fumo");
        wrapFumoItemModel(event, "pigmee_fumo_colored");

        // 彩虹猪咪：包一层 isBuiltInRenderer=true 的模型——1.12.2 只有内建渲染模型的
        // 物品才会走 TEISR，否则 RenderItem 直接按普通贴图渲染
        ModelResourceLocation rainbowMrl = new ModelResourceLocation(Tags.MOD_ID + ":rainbow_pigmee_fumo", "inventory");
        IBakedModel rainbowOriginal = event.getModelRegistry().getObject(rainbowMrl);
        if (rainbowOriginal != null) {
            event.getModelRegistry().putObject(rainbowMrl,
                new net.minecraftforge.client.model.BakedModelWrapper<IBakedModel>(rainbowOriginal) {
                    @Override
                    public boolean isBuiltInRenderer() {
                        return true;
                    }

                    @Override
                    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
                            ItemCameraTransforms.TransformType cameraTransformType) {
                        Pair<? extends IBakedModel, Matrix4f> base = originalModel.handlePerspective(cameraTransformType);
                        return Pair.of(this, base.getRight());
                    }
                });
        }
    }

    private static void wrapFumoItemModel(ModelBakeEvent event, String itemId) {
        ModelResourceLocation mrl = new ModelResourceLocation(Tags.MOD_ID + ":" + itemId, "inventory");
        IBakedModel original = event.getModelRegistry().getObject(mrl);
        if (original != null) {
            event.getModelRegistry().putObject(mrl, new SpinningFumoBakedModel(original));
        }
    }
}
