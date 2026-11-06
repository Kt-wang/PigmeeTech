/*
 * AE2 Crystal Science - TileFumo (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.blockentity.FumoBlockEntity.
 *
 * 保存旋转开关状态（NBT + 数据包同步），客户端每 tick 推进旋转角。
 */
package pigtech.tile.fumo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import javax.annotation.Nullable;

public class TileFumo extends TileEntity implements ITickable {

    public static final float SPIN_DEGREES_PER_TICK = 6.0F;

    private static final String TAG_SPINNING = "Spinning";
    private static final String TAG_COLOR = "Color";
    private static final String TAG_CUSTOM_NAME = "CustomName";

    private boolean spinning;
    /** 16 色猪咪的颜色（EnumDyeColor metadata 0-15）；-1 = 普通/彩虹猪咪 */
    private int color = -1;
    @Nullable
    private String customName;
    private float yRot;
    private float prevYRot;

    public boolean isSpinning() {
        return spinning;
    }

    /** 玩家用命名牌/铁砧命名后放置时保存的名字（jeb_ 彩蛋触发条件） */
    @Nullable
    public String getCustomName() {
        return customName;
    }

    public void setCustomName(@Nullable String name) {
        customName = name;
        markDirty();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    public float getRenderYRot(float partialTick) {
        return prevYRot + (yRot - prevYRot) * partialTick;
    }

    public void toggleSpinning() {
        spinning = !spinning;
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    /** 源码仅客户端 tick（FumoBlock#getTicker 只给客户端），这里同样只在客户端推进角度。 */
    @Override
    public void update() {
        if (world == null || !world.isRemote) {
            return;
        }
        prevYRot = yRot;
        if (spinning) {
            yRot += SPIN_DEGREES_PER_TICK;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        spinning = compound.getBoolean(TAG_SPINNING);
        color = compound.hasKey(TAG_COLOR, 99) ? compound.getInteger(TAG_COLOR) : -1;
        customName = compound.hasKey(TAG_CUSTOM_NAME, 8) ? compound.getString(TAG_CUSTOM_NAME) : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean(TAG_SPINNING, spinning);
        if (color >= 0) {
            compound.setInteger(TAG_COLOR, color);
        }
        if (customName != null) {
            compound.setString(TAG_CUSTOM_NAME, customName);
        }
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setBoolean(TAG_SPINNING, spinning);
        if (color >= 0) {
            tag.setInteger(TAG_COLOR, color);
        }
        if (customName != null) {
            tag.setString(TAG_CUSTOM_NAME, customName);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        spinning = tag.getBoolean(TAG_SPINNING);
        color = tag.hasKey(TAG_COLOR, 99) ? tag.getInteger(TAG_COLOR) : -1;
        customName = tag.hasKey(TAG_CUSTOM_NAME, 8) ? tag.getString(TAG_CUSTOM_NAME) : null;
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }
}
