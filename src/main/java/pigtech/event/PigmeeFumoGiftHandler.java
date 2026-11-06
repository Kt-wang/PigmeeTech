/*
 * AE2 Crystal Science - PigmeeFumoGiftHandler (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.event.PigmeeFumoGiftHandler.
 *
 * 玩家首次进服赠送一只猪咪：优先放入快捷栏，其次背包，满了则掉落在脚边
 * （无拾取延迟、不会消失）。赠送标记写入玩家持久 NBT，死亡重生后保留。
 * 可用配置项 pigmeeFumoGiftOnFirstJoin 关闭。
 */
package pigtech.event;

import pigtech.PigmeeConfig;
import pigtech.Tags;
import pigtech.init.PigmeeBlocks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class PigmeeFumoGiftHandler {

    private static final String GIFTED_TAG = "pigtech.pigmee_fumo_gifted";
    private static final int HOTBAR_START = 0;
    private static final int HOTBAR_END_EXCLUSIVE = 9;

    private PigmeeFumoGiftHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof FakePlayer || !PigmeeConfig.pigmeeFumoGiftOnFirstJoin) {
            return;
        }

        EntityPlayer player = event.player;
        NBTTagCompound data = player.getEntityData();
        if (data.getBoolean(GIFTED_TAG)) {
            return;
        }

        ItemStack gift = new ItemStack(PigmeeBlocks.PIGMEE_FUMO);
        insertGift(player.inventory, gift);
        if (!gift.isEmpty()) {
            dropOverflow(player, gift);
        }
        player.inventory.markDirty();
        data.setBoolean(GIFTED_TAG, true);
    }

    /** 死亡重生后保留"已赠送"标记（对齐源码 PlayerEvent.Clone 处理） */
    @SubscribeEvent
    public static void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        if (event.getOriginal().getEntityData().getBoolean(GIFTED_TAG)) {
            event.getEntityPlayer().getEntityData().setBoolean(GIFTED_TAG, true);
        }
    }

    private static void insertGift(InventoryPlayer inventory, ItemStack gift) {
        tryInsertIntoSlots(inventory, gift, HOTBAR_START, HOTBAR_END_EXCLUSIVE);
        if (!gift.isEmpty()) {
            tryInsertIntoSlots(inventory, gift, HOTBAR_END_EXCLUSIVE, inventory.mainInventory.size());
        }
    }

    private static void dropOverflow(EntityPlayer player, ItemStack gift) {
        ItemStack dropStack = gift.copy();
        gift.setCount(0);
        EntityItem dropped = player.dropItem(dropStack, false, false);
        if (dropped != null) {
            dropped.setNoPickupDelay();
            dropped.setNoDespawn();
        }
    }

    private static void tryInsertIntoSlots(InventoryPlayer inventory, ItemStack gift, int start, int endExclusive) {
        for (int slot = start; slot < endExclusive && !gift.isEmpty(); slot++) {
            ItemStack stack = inventory.mainInventory.get(slot);
            if (canMerge(stack, gift)) {
                int amount = Math.min(gift.getCount(), stack.getMaxStackSize() - stack.getCount());
                stack.grow(amount);
                gift.shrink(amount);
            }
        }

        for (int slot = start; slot < endExclusive && !gift.isEmpty(); slot++) {
            if (inventory.mainInventory.get(slot).isEmpty()) {
                inventory.mainInventory.set(slot, gift.copy());
                gift.setCount(0);
            }
        }
    }

    private static boolean canMerge(ItemStack stack, ItemStack gift) {
        return !stack.isEmpty()
                && ItemStack.areItemsEqual(stack, gift)
                && ItemStack.areItemStackTagsEqual(stack, gift)
                && stack.getCount() < stack.getMaxStackSize();
    }
}
