/*
 * AE2 Crystal Science - CtmFaceGeometry (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.client.ctm.CtmFaceGeometry.
 *
 * Mekanism 风格 compact CTM 的逐面几何。面角点与面内邻居方向移植自源码
 * （其本身移植自 AE2 GlassBakedModel/RenderHelper，UV 朝向与原版方块面一致）。
 * CORNERS[side] = 纹理空间 [TL, BL, BR, TR]；NEIGHBORS[side] = [上,右,下,左] 世界方向。
 * 1.12.2 适配：用 UnpackedBakedQuad.Builder（ITEM 顶点格式）手工构建四边形。
 */
package pigtech.client.ctm;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import javax.vecmath.Vector3f;
import java.util.EnumMap;

public final class CtmFaceGeometry {

    // NEIGHBORS[] 与 edgeConnect 位掩码的边索引
    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;

    private static final EnumMap<EnumFacing, Vector3f[]> CORNERS = buildCorners();
    private static final EnumMap<EnumFacing, EnumFacing[]> NEIGHBORS = buildNeighbors();

    private CtmFaceGeometry() {
    }

    /** 面内指定边（上/右/下/左）对应的世界方向 */
    public static EnumFacing neighborDir(EnumFacing face, int edge) {
        return NEIGHBORS.get(face)[edge];
    }

    /** 面象限对应的对角邻居世界位置 */
    static BlockPos cornerPos(BlockPos pos, EnumFacing face, CtmTileSelector.Quadrant quadrant) {
        switch (quadrant) {
            case TOP_LEFT:
                return pos.offset(neighborDir(face, UP)).offset(neighborDir(face, LEFT));
            case TOP_RIGHT:
                return pos.offset(neighborDir(face, UP)).offset(neighborDir(face, RIGHT));
            case BOTTOM_RIGHT:
                return pos.offset(neighborDir(face, DOWN)).offset(neighborDir(face, RIGHT));
            default:
                return pos.offset(neighborDir(face, DOWN)).offset(neighborDir(face, LEFT));
        }
    }

    /** 使用整个 base 贴图（UV 0..1）的整面四边形 */
    public static BakedQuad fullFace(EnumFacing side, TextureAtlasSprite sprite) {
        Vector3f[] c = CORNERS.get(side);
        return quad(side, c[0], c[1], c[2], c[3], sprite, 0f, 0f, 1f, 1f);
    }

    /** 一个象限的子四边形；sq,tq 选择面的四分之一 */
    static BakedQuad quadrant(EnumFacing side, int sq, int tq, CtmTileSelector.Tile tile,
            TextureAtlasSprite sprite) {
        Vector3f[] c = CORNERS.get(side);
        float s0 = sq * 0.5f, s1 = s0 + 0.5f;
        float t0 = tq * 0.5f, t1 = t0 + 0.5f;
        Vector3f tl = lerp(c, s0, t0);
        Vector3f bl = lerp(c, s0, t1);
        Vector3f br = lerp(c, s1, t1);
        Vector3f tr = lerp(c, s1, t0);
        float step = 1f / tile.source.gridSize();
        float u0 = tile.x * step, u1 = u0 + step;
        float v0 = tile.y * step, v1 = v0 + step;
        return quad(side, tl, bl, br, tr, sprite, u0, v0, u1, v1);
    }

    // 面上的双线性点：P = TL + s*(TR-TL) + t*(BL-TL)。c = [TL, BL, BR, TR]。
    private static Vector3f lerp(Vector3f[] c, float s, float t) {
        Vector3f tl = c[0], bl = c[1], tr = c[3];
        return new Vector3f(
                tl.x + s * (tr.x - tl.x) + t * (bl.x - tl.x),
                tl.y + s * (tr.y - tl.y) + t * (bl.y - tl.y),
                tl.z + s * (tr.z - tl.z) + t * (bl.z - tl.z));
    }

    private static BakedQuad quad(EnumFacing side, Vector3f tl, Vector3f bl, Vector3f br, Vector3f tr,
            TextureAtlasSprite sprite, float u0, float v0, float u1, float v1) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
        builder.setQuadOrientation(side);
        builder.setTexture(sprite);
        builder.setApplyDiffuseLighting(true);
        // 顶点顺序 tl, bl, br, tr（对齐源码）；法线由 setQuadOrientation 在 build 时自动填充
        putVertex(builder, tl, sprite, u0, v0, side);
        putVertex(builder, bl, sprite, u0, v1, side);
        putVertex(builder, br, sprite, u1, v1, side);
        putVertex(builder, tr, sprite, u1, v0, side);
        return builder.build();
    }

    private static void putVertex(UnpackedBakedQuad.Builder builder, Vector3f pos,
            TextureAtlasSprite sprite, float u, float v, EnumFacing side) {
        builder.put(0, pos.x, pos.y, pos.z, 0);
        builder.put(1, 1, 1, 1, 1);
        builder.put(2, sprite.getInterpolatedU(u * 16), sprite.getInterpolatedV(v * 16), 0, 0);
        builder.put(3, side.getXOffset(), side.getYOffset(), side.getZOffset(), 0);
        builder.put(4, 0, 0, 0, 0);
    }

    // 面角点 [TL, BL, BR, TR]（纹理空间，移植自源码/原版方块面朝向约定）
    private static EnumMap<EnumFacing, Vector3f[]> buildCorners() {
        EnumMap<EnumFacing, Vector3f[]> map = new EnumMap<>(EnumFacing.class);
        for (EnumFacing facing : EnumFacing.VALUES) {
            float o = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 0f : 1f;
            Vector3f[] corners;
            switch (facing.getAxis()) {
                case X:
                    corners = new Vector3f[]{new Vector3f(o, 1, 1), new Vector3f(o, 0, 1),
                            new Vector3f(o, 0, 0), new Vector3f(o, 1, 0)};
                    break;
                case Y:
                    corners = new Vector3f[]{new Vector3f(1, o, 1), new Vector3f(1, o, 0),
                            new Vector3f(0, o, 0), new Vector3f(0, o, 1)};
                    break;
                default:
                    corners = new Vector3f[]{new Vector3f(0, 1, o), new Vector3f(0, 0, o),
                            new Vector3f(1, 0, o), new Vector3f(1, 1, o)};
                    break;
            }
            if (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                corners = new Vector3f[]{corners[3], corners[2], corners[1], corners[0]};
            }
            map.put(facing, corners);
        }
        return map;
    }

    // 面内 [上, 右, 下, 左] 的世界方向（移植自源码/AE2 makeBitmask）
    private static EnumMap<EnumFacing, EnumFacing[]> buildNeighbors() {
        EnumMap<EnumFacing, EnumFacing[]> map = new EnumMap<>(EnumFacing.class);
        map.put(EnumFacing.DOWN, new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.WEST});
        map.put(EnumFacing.UP, new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST});
        map.put(EnumFacing.NORTH, new EnumFacing[]{EnumFacing.UP, EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.EAST});
        map.put(EnumFacing.SOUTH, new EnumFacing[]{EnumFacing.UP, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.WEST});
        map.put(EnumFacing.WEST, new EnumFacing[]{EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.NORTH});
        map.put(EnumFacing.EAST, new EnumFacing[]{EnumFacing.UP, EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.SOUTH});
        return map;
    }
}
