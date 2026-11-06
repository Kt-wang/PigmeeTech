/*
 * AE2 Crystal Science - RainbowPigmeeHandler (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.lightning.RainbowPigmeeTransformation.
 *
 * 命名为 jeb_ 的猪咪（掉落物或已放置方块）被雷击中后转化为彩虹猪咪。
 * 1.12.2 没有雷击事件总线回调，改用 EntityJoinWorldEvent 监听闪电生成并扫描范围。
 */
package pigtech.event;

import pigtech.block.fumo.FumoBlock;
import pigtech.init.PigmeeBlocks;
import pigtech.tile.fumo.TileFumo;
import pigtech.Tags;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class RainbowPigmeeHandler {

    public static final String REQUIRED_NAME = "jeb_";

    /** 对齐源码 LightningTransformRules：水平 3 格、垂直 2 格 */
    private static final double SEARCH_HORIZONTAL_RADIUS = 3.0D;
    private static final double SEARCH_VERTICAL_RADIUS = 2.0D;
    private static final String CHECKED_TAG = "pigtech:rainbow_checked";
    private static final String PROTECTED_UNTIL = "pigtech:rainbow_protected_until";
    private static final String WAS_INVULNERABLE = "pigtech:rainbow_was_invulnerable";
    private static final java.util.Set<EntityItem> PROTECTED_ITEMS =
            java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

    private RainbowPigmeeHandler() {
    }

    public static boolean matchesName(@Nullable String name) {
        return REQUIRED_NAME.equals(name);
    }

    @SubscribeEvent
    public static void onLightningSpawn(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (event.getEntity() instanceof EntityItem item && item.getEntityData().hasKey(PROTECTED_UNTIL)) {
            PROTECTED_ITEMS.add(item);
        }
        if (event.getEntity() instanceof EntityLightningBolt bolt) handleLightning(bolt);
    }

    /** Weather effects are not ordinary spawned entities on 1.12.2. */
    @SubscribeEvent
    public static void onWorldTick(net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.START || event.world.isRemote) return;
        for (net.minecraft.entity.Entity entity : event.world.weatherEffects)
            if (entity instanceof EntityLightningBolt bolt) handleLightning(bolt);
        var iterator = PROTECTED_ITEMS.iterator();
        while (iterator.hasNext()) {
            EntityItem item = iterator.next();
            if (item.world != event.world) continue;
            if (item.isDead) { iterator.remove(); continue; }
            NBTTagCompound data = item.getEntityData();
            if (data.getLong(PROTECTED_UNTIL) <= event.world.getTotalWorldTime()) {
                item.setEntityInvulnerable(data.getBoolean(WAS_INVULNERABLE));
                data.removeTag(PROTECTED_UNTIL);
                data.removeTag(WAS_INVULNERABLE);
                iterator.remove();
            } else item.extinguish();
        }
    }

    public static void handleLightning(EntityLightningBolt bolt) {
        if (bolt.world.isRemote || bolt.getEntityData().getBoolean(CHECKED_TAG)) return;
        bolt.getEntityData().setBoolean(CHECKED_TAG, true);
        World world = bolt.world;
        BlockPos center = new BlockPos(bolt.posX, bolt.posY, bolt.posZ);
        AxisAlignedBB box = new AxisAlignedBB(center)
                .grow(SEARCH_HORIZONTAL_RADIUS, SEARCH_VERTICAL_RADIUS, SEARCH_HORIZONTAL_RADIUS);

        convertItemEntities(world, box);
        convertPlacedBlocks(world, center);
    }

    /** 掉落物形态：jeb_ 猪咪 → 彩虹猪咪，清除名字并保护产物不被同一道雷烧毁 */
    private static void convertItemEntities(World world, AxisAlignedBB box) {
        Item pigmeeItem = Item.getItemFromBlock(PigmeeBlocks.PIGMEE_FUMO);
        for (EntityItem entity : world.getEntitiesWithinAABB(EntityItem.class, box)) {
            ItemStack stack = entity.getItem();
            if (stack.getItem() != pigmeeItem || !stack.hasDisplayName() || !matchesName(stack.getDisplayName())) {
                continue;
            }
            ItemStack result = new ItemStack(Item.getItemFromBlock(PigmeeBlocks.RAINBOW_PIGMEE_FUMO), stack.getCount());
            if (stack.hasTagCompound()) {
                NBTTagCompound tag = stack.getTagCompound().copy();
                if (tag.hasKey("display", 10)) {
                    NBTTagCompound display = tag.getCompoundTag("display");
                    display.removeTag("Name");
                    if (display.isEmpty()) {
                        tag.removeTag("display");
                    }
                }
                if (!tag.isEmpty()) {
                    result.setTagCompound(tag);
                }
            }
            entity.setItem(result);
            // 对齐源码的输出保护：刚转化的产物必须躲过这道雷接下来的伤害判定
            NBTTagCompound data = entity.getEntityData();
            data.setBoolean(WAS_INVULNERABLE, entity.getIsInvulnerable());
            data.setLong(PROTECTED_UNTIL, world.getTotalWorldTime() + 40);
            entity.setEntityInvulnerable(true);
            entity.extinguish();
            PROTECTED_ITEMS.add(entity);
        }
    }

    /** 方块形态：保留朝向与旋转状态，清除名字（显示新的译名） */
    private static void convertPlacedBlocks(World world, BlockPos center) {
        int h = (int) SEARCH_HORIZONTAL_RADIUS;
        int v = (int) SEARCH_VERTICAL_RADIUS;
        for (BlockPos pos : BlockPos.getAllInBox(center.add(-h, -v, -h), center.add(h, v, h))) {
            // 不为了彩蛋加载邻近区块（对齐源码 hasChunkAt）
            if (!world.isBlockLoaded(pos)) {
                continue;
            }
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() != PigmeeBlocks.PIGMEE_FUMO) {
                continue;
            }
            TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof TileFumo) || !matchesName(((TileFumo) te).getCustomName())) {
                continue;
            }
            boolean spinning = ((TileFumo) te).isSpinning();
            IBlockState result = PigmeeBlocks.RAINBOW_PIGMEE_FUMO.getDefaultState()
                    .withProperty(FumoBlock.FACING, state.getValue(FumoBlock.FACING));
            // Preserve optional waterlogging properties injected by fluid compatibility mods.
            for (net.minecraft.block.properties.IProperty<?> property : state.getPropertyKeys())
                if (property.getName().equals("waterlogged") && result.getPropertyKeys().contains(property))
                    result = copyProperty(result, state, property);
            world.setBlockState(pos, result, 3);
            TileEntity converted = world.getTileEntity(pos);
            if (converted instanceof TileFumo && ((TileFumo) converted).isSpinning() != spinning) {
                ((TileFumo) converted).toggleSpinning();
            }
        }
    }

    private static <T extends Comparable<T>> IBlockState copyProperty(IBlockState target, IBlockState source,
                                                                    net.minecraft.block.properties.IProperty<T> property) {
        return target.withProperty(property, source.getValue(property));
    }
}
