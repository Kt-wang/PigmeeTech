/*
 * Pigmee Tech - Block / ItemBlock registration (split from AE2CS Blocks)
 */
package pigtech.init;

import pigtech.PigmeeTech;
import pigtech.Tags;
import pigtech.block.building.PigmeeBuildingBlock;
import pigtech.block.building.PigmeeBuildingPanelBlock;
import pigtech.block.building.PigmeeBuildingSlabBlock;
import pigtech.block.fumo.ColoredFumoBlock;
import pigtech.block.fumo.FumoBlock;
import pigtech.item.fumo.ColoredFumoItemBlock;
import pigtech.item.fumo.FumoItemBlock;

import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PigmeeBlocks {

    // 猪咪玩偶
    public static FumoBlock PIGMEE_FUMO;
    public static FumoBlock CREATIVE_PIGMEE_FUMO;
    public static FumoBlock RAINBOW_PIGMEE_FUMO;
    /** 16 色猪咪（颜色存于 TileFumo，物品 metadata = EnumDyeColor metadata） */
    public static ColoredFumoBlock PIGMEE_FUMO_COLORED;

    // 猪咪建材
    /** 注册名与源码/彩虹猪咪颜色族一致：1.21 风格（light_gray，而非 1.12 的 silver） */
    public static final String[] PIGMEE_COLOR_NAMES = {
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    public static PigmeeBuildingBlock PIGMEE_BUILDING_BLOCK;
    public static final Map<EnumDyeColor, PigmeeBuildingPanelBlock> PIGMEE_BUILDING_PANELS =
        new EnumMap<>(EnumDyeColor.class);
    public static final Map<EnumDyeColor, PigmeeBuildingPanelBlock> PIGMEE_FRAMED_BUILDING_PANELS =
        new EnumMap<>(EnumDyeColor.class);
    public static PigmeeBuildingSlabBlock.Half PIGMEE_BUILDING_SLAB;
    public static final Map<EnumDyeColor, PigmeeBuildingSlabBlock.Half> PIGMEE_BUILDING_SLABS =
        new EnumMap<>(EnumDyeColor.class);
    public static final Map<EnumDyeColor, PigmeeBuildingSlabBlock.Half> PIGMEE_FRAMED_BUILDING_SLABS =
        new EnumMap<>(EnumDyeColor.class);
    /** [half, double] 对，供 registerItemBlocks 注册 ItemSlab */
    private static final List<PigmeeBuildingSlabBlock[]> PIGMEE_SLAB_PAIRS = new ArrayList<>();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        PIGMEE_FUMO = reg(event, new FumoBlock(), "pigmee_fumo");
        PIGMEE_FUMO.setCreativeTab(PigmeeTab.INSTANCE);
        CREATIVE_PIGMEE_FUMO = reg(event, new FumoBlock(), "creative_pigmee_fumo");
        CREATIVE_PIGMEE_FUMO.setCreativeTab(PigmeeTab.INSTANCE);
        RAINBOW_PIGMEE_FUMO = reg(event, new FumoBlock(), "rainbow_pigmee_fumo");
        RAINBOW_PIGMEE_FUMO.setCreativeTab(PigmeeTab.INSTANCE);
        PIGMEE_FUMO_COLORED = reg(event, new ColoredFumoBlock(), "pigmee_fumo_colored");
        PIGMEE_FUMO_COLORED.setCreativeTab(PigmeeTab.INSTANCE);

        PIGMEE_BUILDING_BLOCK = reg(event, new PigmeeBuildingBlock(), "pigmee_building_block");
        PIGMEE_BUILDING_BLOCK.setCreativeTab(PigmeeTab.INSTANCE);
        for (EnumDyeColor color : EnumDyeColor.values()) {
            String cname = PIGMEE_COLOR_NAMES[color.getMetadata()];
            PigmeeBuildingPanelBlock panel = reg(event,
                new PigmeeBuildingPanelBlock(color), cname + "_pigmee_building_panel");
            panel.setCreativeTab(PigmeeTab.INSTANCE);
            PIGMEE_BUILDING_PANELS.put(color, panel);
            PigmeeBuildingPanelBlock framed = reg(event,
                new PigmeeBuildingPanelBlock(color), cname + "_pigmee_framed_building_panel");
            framed.setCreativeTab(PigmeeTab.INSTANCE);
            PIGMEE_FRAMED_BUILDING_PANELS.put(color, framed);
        }
        // 台阶：1.12.2 需要 半砖+双砖 成对注册（双砖无物品）
        PIGMEE_BUILDING_SLAB = regSlabPair(event, "pigmee_building_slab");
        for (EnumDyeColor color : EnumDyeColor.values()) {
            String cname = PIGMEE_COLOR_NAMES[color.getMetadata()];
            PIGMEE_BUILDING_SLABS.put(color,
                regSlabPair(event, cname + "_pigmee_building_slab"));
            PIGMEE_FRAMED_BUILDING_SLABS.put(color,
                regSlabPair(event, cname + "_pigmee_framed_building_slab"));
        }
        PigmeeTech.LOGGER.info("Registered pigmee blocks");
    }

    private static PigmeeBuildingSlabBlock.Half regSlabPair(RegistryEvent.Register<Block> event,
                                                            String name) {
        PigmeeBuildingSlabBlock.Half half = new PigmeeBuildingSlabBlock.Half();
        PigmeeBuildingSlabBlock.Double dbl = new PigmeeBuildingSlabBlock.Double(half);
        reg(event, half, name);
        reg(event, dbl, name + "_double");
        half.setCreativeTab(PigmeeTab.INSTANCE);
        dbl.setCreativeTab(null);  // 双砖无物品，不进创造栏
        dbl.setTranslationKey(name);  // 与半砖共用译名
        PIGMEE_SLAB_PAIRS.add(new PigmeeBuildingSlabBlock[]{half, dbl});
        return half;
    }

    private static <T extends Block> T reg(RegistryEvent.Register<Block> evt, T block, String name) {
        block.setRegistryName(Tags.MOD_ID, name);
        block.setTranslationKey(name);
        block.setCreativeTab(PigmeeTab.INSTANCE);
        evt.getRegistry().register(block);
        return block;
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        PigmeeTech.LOGGER.info("Registering pigmee item blocks...");
        // 猪咪玩偶：自定义方块物品（4 行 tooltip、可戴头上），创造猪咪为 EPIC 稀有度
        regFumoItem(event, new FumoItemBlock(PIGMEE_FUMO, "tooltip.pigtech.pigmee_fumo"), "pigmee_fumo");
        regFumoItem(event, new FumoItemBlock(CREATIVE_PIGMEE_FUMO,
            "tooltip.pigtech.creative_pigmee_fumo", EnumRarity.EPIC), "creative_pigmee_fumo");
        regFumoItem(event, new FumoItemBlock(RAINBOW_PIGMEE_FUMO,
            "tooltip.pigtech.rainbow_pigmee_fumo", EnumRarity.UNCOMMON), "rainbow_pigmee_fumo");
        regFumoItem(event, new ColoredFumoItemBlock(PIGMEE_FUMO_COLORED), "pigmee_fumo_colored");

        // 猪咪建材：普通方块物品 + 台阶的 ItemSlab
        regBuildingItem(event, PIGMEE_BUILDING_BLOCK, "pigmee_building_block");
        for (PigmeeBuildingPanelBlock panel : PIGMEE_BUILDING_PANELS.values()) {
            regBuildingItem(event, panel, panel.getRegistryName().getPath());
        }
        for (PigmeeBuildingPanelBlock framed : PIGMEE_FRAMED_BUILDING_PANELS.values()) {
            regBuildingItem(event, framed, framed.getRegistryName().getPath());
        }
        for (PigmeeBuildingSlabBlock[] pair : PIGMEE_SLAB_PAIRS) {
            ItemSlab slabItem = new ItemSlab(pair[0], pair[0], pair[1]);
            slabItem.setRegistryName(pair[0].getRegistryName());
            slabItem.setCreativeTab(PigmeeTab.INSTANCE);
            event.getRegistry().register(slabItem);
        }
    }

    private static void regBuildingItem(RegistryEvent.Register<Item> event, Block block, String name) {
        ItemBlock item = new ItemBlock(block);
        item.setRegistryName(Tags.MOD_ID, name);
        item.setCreativeTab(PigmeeTab.INSTANCE);
        event.getRegistry().register(item);
    }

    private static void regFumoItem(RegistryEvent.Register<Item> event, Item item, String name) {
        item.setRegistryName(Tags.MOD_ID, name);
        item.setCreativeTab(PigmeeTab.INSTANCE);
        event.getRegistry().register(item);
    }
}
