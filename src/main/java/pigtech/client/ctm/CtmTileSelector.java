/*
 * Pigmee Tech - CtmTileSelector (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.client.ctm.CtmTileSelector（纯逻辑，逐字移植）。
 *
 * 为一个面的一个象限选择 CTM compact 子图。CTM 贴图是 4×4 网格的 8×8 子图；
 * 无正交连接的角落采样自原始 16×16 base 贴图的 2×2 象限网格。
 */
package pigtech.client.ctm;

import static pigtech.client.ctm.CtmFaceGeometry.DOWN;
import static pigtech.client.ctm.CtmFaceGeometry.LEFT;
import static pigtech.client.ctm.CtmFaceGeometry.RIGHT;
import static pigtech.client.ctm.CtmFaceGeometry.UP;

final class CtmTileSelector {

    enum Source {
        BASE(2),
        CTM(4);

        private final int gridSize;

        Source(int gridSize) {
            this.gridSize = gridSize;
        }

        int gridSize() {
            return gridSize;
        }
    }

    enum Quadrant {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }

    static final class Tile {
        final Source source;
        final int x;
        final int y;

        Tile(Source source, int x, int y) {
            this.source = source;
            this.x = x;
            this.y = y;
        }
    }

    private CtmTileSelector() {
    }

    static Quadrant quadrant(int sq, int tq) {
        if (tq == 0) {
            return sq == 0 ? Quadrant.TOP_LEFT : Quadrant.TOP_RIGHT;
        }
        return sq == 0 ? Quadrant.BOTTOM_LEFT : Quadrant.BOTTOM_RIGHT;
    }

    static Tile select(Quadrant quadrant, int edgeMask, int cornerMask) {
        switch (quadrant) {
            case TOP_LEFT:
                return selectCorner(quadrant, edgeMask, cornerMask, UP, LEFT,
                        0, 0, 0, 0, 2, 0, 0, 2, 2, 2);
            case TOP_RIGHT:
                return selectCorner(quadrant, edgeMask, cornerMask, UP, RIGHT,
                        1, 0, 1, 0, 3, 0, 1, 2, 3, 2);
            case BOTTOM_LEFT:
                return selectCorner(quadrant, edgeMask, cornerMask, DOWN, LEFT,
                        0, 1, 0, 1, 2, 1, 0, 3, 2, 3);
            default:
                return selectCorner(quadrant, edgeMask, cornerMask, DOWN, RIGHT,
                        1, 1, 1, 1, 3, 1, 1, 3, 3, 3);
        }
    }

    // 两条边都连接时还需对角落连接才取内角子图，否则取缺对角子图（对齐源码）
    private static Tile selectCorner(Quadrant diagonal, int edgeMask, int cornerMask, int firstEdge, int secondEdge,
            int baseX, int baseY, int interiorX, int interiorY, int firstOnlyX, int firstOnlyY,
            int secondOnlyX, int secondOnlyY, int missingDiagonalX, int missingDiagonalY) {
        boolean firstConnected = has(edgeMask, firstEdge);
        boolean secondConnected = has(edgeMask, secondEdge);
        if (firstConnected && secondConnected) {
            return has(cornerMask, diagonal.ordinal())
                    ? new Tile(Source.CTM, interiorX, interiorY)
                    : new Tile(Source.CTM, missingDiagonalX, missingDiagonalY);
        }
        if (firstConnected) {
            return new Tile(Source.CTM, firstOnlyX, firstOnlyY);
        }
        if (secondConnected) {
            return new Tile(Source.CTM, secondOnlyX, secondOnlyY);
        }
        return new Tile(Source.BASE, baseX, baseY);
    }

    private static boolean has(int mask, int bit) {
        return (mask & (1 << bit)) != 0;
    }
}
