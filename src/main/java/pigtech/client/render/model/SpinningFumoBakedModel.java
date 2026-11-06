/*
 * AE2 Crystal Science - SpinningFumoBakedModel (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.client.SpinningFumoBakedModel.
 *
 * 仅当物品以 HEAD 变换渲染时（戴在头上），绕中心 Y 轴持续旋转；
 * 其余场景（背包/GUI/手持/掉落物）完全委托给原模型。
 *
 * 注：本 Fork 的 Cleanroom 已将 IPerspectiveAwareModel 并入 IBakedModel，
 * handlePerspective 是 IBakedModel 的默认方法。
 */
package pigtech.client.render.model;

import pigtech.tile.fumo.TileFumo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;

public class SpinningFumoBakedModel extends BakedModelWrapper<IBakedModel> {

    public SpinningFumoBakedModel(IBakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        Pair<? extends IBakedModel, Matrix4f> base = originalModel.handlePerspective(cameraTransformType);
        if (cameraTransformType != ItemCameraTransforms.TransformType.HEAD) {
            return base;
        }

        float angle = 0.0F;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            float partialTick = mc.getRenderPartialTicks();
            angle = ((mc.world.getTotalWorldTime() % 60L) + partialTick) * TileFumo.SPIN_DEGREES_PER_TICK;
        }

        Matrix4f rotation = new Matrix4f();
        rotation.rotY((float) Math.toRadians(angle));
        Matrix4f result = base.getRight() == null ? new Matrix4f() : new Matrix4f(base.getRight());
        result.mul(rotation);
        return Pair.of(this, result);
    }
}
