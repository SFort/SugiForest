package sugiforest.block;

import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sugiforest.core.SugiForest;
import sugiforest.item.SugiItems;

public class BlockSugiLog extends BlockLog
{
	public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);

	public BlockSugiLog()
	{
		super();
		this.setUnlocalizedName("log.sugi");
		this.setHarvestLevel("axe", 0);
		this.setDefaultState(blockState.getBaseState().withProperty(VARIANT, EnumType.NORMAL).withProperty(LOG_AXIS, EnumAxis.Y));
		this.setCreativeTab(SugiForest.TAB_SUGI);
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, VARIANT, LOG_AXIS);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		IBlockState state = getDefaultState().withProperty(VARIANT, (meta & 3) % 4 == 1 ? EnumType.MYST : EnumType.NORMAL);

		switch (meta & 12)
		{
			case 0:
				return state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Y);
			case 4:
				return state.withProperty(LOG_AXIS, BlockLog.EnumAxis.X);
			case 8:
				return state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Z);
			default:
				return state.withProperty(LOG_AXIS, BlockLog.EnumAxis.NONE);
		}
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		int meta = 0;

		meta = meta | state.getValue(VARIANT).getMetadata();

		switch (state.getValue(LOG_AXIS))
		{
			case X:
				return meta | 4;
			case Z:
				return meta | 8;
			case NONE:
				return meta | 12;
			default:
				return meta;
		}
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess blockAccess, BlockPos pos)
	{
		return BlockPlanks.EnumType.BIRCH.getMapColor();
	}

	@Override
	public int getLightValue(IBlockState state)
	{
		switch (state.getValue(VARIANT))
		{
			case MYST:
				return 5;
			default:
		}

		return super.getLightValue(state);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		boolean ret = super.removedByPlayer(state, world, pos, player, willHarvest);

		if (ret && state.getValue(VARIANT) == EnumType.MYST)
		{
			if (player.inventory.hasItemStack(new ItemStack(Items.BOWL)) && player.inventory.clearMatchingItems(Items.BOWL, 0, 1, null) >= 1)
			{
				spawnAsEntity(world, pos, new ItemStack(SugiItems.MYST_SAP));
			}
		}

		return ret;
	}

	public enum EnumType implements IStringSerializable
	{
		NORMAL(0, "normal"),
		MYST(1, "myst");

		private static final EnumType[] VALUES = new EnumType[values().length];

		private final int meta;
		private final String name;

		private EnumType(int meta, String name)
		{
			this.meta = meta;
			this.name = name;
		}

		public int getMetadata()
		{
			return meta;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public String toString()
		{
			return name;
		}

		public static EnumType byMetadata(int meta)
		{
			if (meta < 0 || meta >= VALUES.length)
			{
				meta = 0;
			}

			return VALUES[meta];
		}

		static
		{
			for (EnumType type : values())
			{
				VALUES[type.getMetadata()] = type;
			}
		}
	}
}