package sugiforest.block;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import sugiforest.core.SugiForest;

public class BlockSugiFenceGate extends BlockFenceGate
{
	public BlockSugiFenceGate()
	{
		super(BlockPlanks.EnumType.BIRCH);
		this.setUnlocalizedName("fenceGate.sugi");
		this.setHardness(1.75F);
		this.setResistance(4.75F);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setCreativeTab(SugiForest.TAB_SUGI);
	}
}