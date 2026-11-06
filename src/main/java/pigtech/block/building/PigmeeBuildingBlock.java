/*
 * Pigmee Tech - PigmeeBuildingBlock (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.block.PigmeeBuildingBlock.
 *
 * 基础猪咪建材：纯装饰方块，石质属性（与原版石质方块同工具需求）。
 */
package pigtech.block.building;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class PigmeeBuildingBlock extends Block {

    public PigmeeBuildingBlock() {
        super(Material.ROCK);
        this.setSoundType(SoundType.STONE);
        this.setHardness(1.5F);
        this.setResistance(6.0F);
        this.setHarvestLevel("pickaxe", 0);
    }
}
