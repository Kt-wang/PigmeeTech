/*
 * AE2 Crystal Science - PigmeeCtm (1.12.2)
 * 猪咪建材 CTM 装配：未列入属性定义、CTM 贴图缝合、ModelBakeEvent 模型替换。
 *
 * 1.12.2 与源码（1.21.1 几何加载器）的机制差异：
 *   - 无 "loader" JSON 分发 → 在 ModelBakeEvent 直接按方块状态 MRL 替换烘焙模型
 *   - ctm 贴图不被任何 JSON 模型引用 → TextureStitchEvent.Pre 手动缝合进图集
 *   - 物品模型仍指向普通整砖模型（物品栏显示无连纹外观，与源码无 ModelData 时一致）
 * 邻接变化的网格重建无需代码：原版 RenderGlobal.notifyBlockUpdate 本就标记 ±1 范围。
 */
package pigtech.client.ctm;

import pigtech.Tags;
import pigtech.init.PigmeeBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class PigmeeCtm {

    /** 连接数据经此未列入属性随 IExtendedBlockState 传给烘焙模型（对应源码 ModelProperty） */
    public static final IUnlistedProperty<CtmConnectionState> CONNECTION = new IUnlistedProperty<CtmConnectionState>() {
        @Override
        public String getName() {
            return "ctm";
        }

        @Override
        public boolean isValid(CtmConnectionState value) {
            return value != null;
        }

        @Override
        public Class<CtmConnectionState> getType() {
            return CtmConnectionState.class;
        }

        @Override
        public String valueToString(CtmConnectionState value) {
            return String.valueOf(value);
        }
    };

    private PigmeeCtm() {
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getMap() != Minecraft.getMinecraft().getTextureMapBlocks()) {
            return;
        }
        for (EnumDyeColor color : EnumDyeColor.values()) {
            String cname = PigmeeBlocks.PIGMEE_COLOR_NAMES[color.getMetadata()];
            event.getMap().registerSprite(new ResourceLocation(Tags.MOD_ID,
                    "block/pigmee_building/" + cname + "_pigmee_building_panel_ctm"));
            event.getMap().registerSprite(new ResourceLocation(Tags.MOD_ID,
                    "block/pigmee_building/" + cname + "_pigmee_framed_building_panel_ctm"));
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        for (EnumDyeColor color : EnumDyeColor.values()) {
            String cname = PigmeeBlocks.PIGMEE_COLOR_NAMES[color.getMetadata()];
            for (boolean framed : new boolean[]{false, true}) {
                String suffix = framed ? "_pigmee_framed_building_panel" : "_pigmee_building_panel";
                String name = cname + suffix;
                TextureAtlasSprite base = map.getAtlasSprite(Tags.MOD_ID + ":block/pigmee_building/" + name);
                TextureAtlasSprite ctm = map.getAtlasSprite(Tags.MOD_ID + ":block/pigmee_building/" + name + "_ctm");

                // 板材：整方块 CTM，同色跨素/框互连
                event.getModelRegistry().putObject(
                        new ModelResourceLocation(Tags.MOD_ID + ":" + name, "normal"),
                        new ConnectedTextureBakedModel(base, ctm, ConnectionPredicates.PIGMEE_SAME_COLOR));

                // 台阶：同块同半值共面连接（沿用板材贴图），bottom/top/double 三个形态
                String slabSuffix = framed ? "_pigmee_framed_building_slab" : "_pigmee_building_slab";
                String slabName = cname + slabSuffix;
                event.getModelRegistry().putObject(
                        new ModelResourceLocation(Tags.MOD_ID + ":" + slabName, "half=bottom,variant=default"),
                        new ConnectedSlabBakedModel(base, ctm, ConnectedSlabBakedModel.SlabKind.BOTTOM));
                event.getModelRegistry().putObject(
                        new ModelResourceLocation(Tags.MOD_ID + ":" + slabName, "half=top,variant=default"),
                        new ConnectedSlabBakedModel(base, ctm, ConnectedSlabBakedModel.SlabKind.TOP));
                event.getModelRegistry().putObject(
                        new ModelResourceLocation(Tags.MOD_ID + ":" + slabName + "_double", "variant=default"),
                        new ConnectedSlabBakedModel(base, ctm, ConnectedSlabBakedModel.SlabKind.DOUBLE));
            }
        }
    }
}
