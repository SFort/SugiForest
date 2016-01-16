/*
 * SugiForest
 *
 * Copyright (c) 2015 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package sugiforest.block;

import java.util.Random;

import com.google.common.cache.LoadingCache;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockPattern.PatternHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sugiforest.api.SugiForestAPI;
import sugiforest.core.SugiForest;
import sugiforest.world.TeleporterSugiForest;

public class BlockSugiPortal extends BlockPortal
{
	public BlockSugiPortal()
	{
		super();
		this.setUnlocalizedName("portal.sugi");
		this.setLightOpacity(3);
		this.setLightLevel(0.5F);
		this.setBlockUnbreakable();
		this.setStepSound(soundTypeGlass);
		this.setTickRandomly(false);
		this.disableStats();
		this.setCreativeTab(SugiForest.tabSugiForest);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {}

	@Override
	public boolean func_176548_d(World worldIn, BlockPos pos)
	{
		Size size = new Size(worldIn, pos, EnumFacing.Axis.X);

		if (size.func_150860_b() && size.field_150864_e == 0)
		{
			size.func_150859_c();

			return true;
		}
		else
		{
			Size size1 = new Size(worldIn, pos, EnumFacing.Axis.Z);

			if (size1.func_150860_b() && size1.field_150864_e == 0)
			{
				size1.func_150859_c();

				return true;
			}
			else return false;
		}
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		EnumFacing.Axis axis = state.getValue(AXIS);
		Size size;

		if (axis == EnumFacing.Axis.X)
		{
			size = new Size(worldIn, pos, EnumFacing.Axis.X);

			if (!size.func_150860_b() || size.field_150864_e < size.field_150868_h * size.field_150862_g)
			{
				worldIn.setBlockState(pos, Blocks.air.getDefaultState());
			}
		}
		else if (axis == EnumFacing.Axis.Z)
		{
			size = new Size(worldIn, pos, EnumFacing.Axis.Z);

			if (!size.func_150860_b() || size.field_150864_e < size.field_150868_h * size.field_150862_g)
			{
				worldIn.setBlockState(pos, Blocks.air.getDefaultState());
			}
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		if (SugiForestAPI.getDimension() == 0)
		{
			return;
		}

		if (!world.isRemote && entity.isEntityAlive())
		{
			if (entity.timeUntilPortal <= 0)
			{
				MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
				int dimOld = entity.dimension;
				int dimNew = dimOld == SugiForestAPI.getDimension() ? entity.getEntityData().getInteger("SugiForest:LastDim") : SugiForestAPI.getDimension();
				WorldServer worldOld = server.worldServerForDimension(dimOld);
				WorldServer worldNew = server.worldServerForDimension(dimNew);

				if (worldOld == null || worldNew == null)
				{
					return;
				}

				Teleporter teleporter = new TeleporterSugiForest(worldNew);

				entity.worldObj.removeEntity(entity);
				entity.isDead = false;
				entity.timeUntilPortal = entity.getPortalCooldown();

//				PatternHelper pattern = Blocks.portal.func_181089_f(entity.worldObj, pos);
//				double d0 = pattern.getFinger().getAxis() == EnumFacing.Axis.X ? (double)pattern.func_181117_a().getZ() : (double)pattern.func_181117_a().getX();
//				double d1 = pattern.getFinger().getAxis() == EnumFacing.Axis.X ? entity.posZ : entity.posX;
//				d1 = Math.abs(MathHelper.func_181160_c(d1 - (pattern.getFinger().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), d0, d0 - pattern.func_181118_d()));
//				double d2 = MathHelper.func_181160_c(entity.posY - 1.0D, pattern.func_181117_a().getY(), pattern.func_181117_a().getY() - pattern.func_181119_e());
//				ObfuscationReflectionHelper.setPrivateValue(Entity.class, entity, new Vec3(d1, d2, 0.0D), "field_" + "181017_ao");
//				ObfuscationReflectionHelper.setPrivateValue(Entity.class, entity, pattern.getFinger(), "field_" + "181018_ap");

				if (entity instanceof EntityPlayerMP)
				{
					EntityPlayerMP player = (EntityPlayerMP)entity;

					if (!player.isSneaking() && !player.isPotionActive(Potion.blindness))
					{
						worldOld.playSoundToNearExcept(player, "sugiforest:sugi_portal", 0.5F, 1.0F);

						server.getConfigurationManager().transferPlayerToDimension(player, dimNew, teleporter);

						worldNew.playSoundAtEntity(player, "sugiforest:sugi_portal", 0.75F, 1.0F);

						player.getEntityData().setInteger("SugiForest:LastDim", dimOld);
					}
				}
				else
				{
					entity.dimension = dimNew;

					server.getConfigurationManager().transferEntityToWorld(entity, dimOld, worldOld, worldNew, teleporter);

					Entity target = EntityList.createEntityByName(EntityList.getEntityString(entity), worldNew);

					if (target != null)
					{
						worldOld.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, "sugiforest:sugi_portal", 0.25F, 1.15F);

						target.copyDataFromOld(entity);
						target.forceSpawn = true;

						worldNew.spawnEntityInWorld(target);
						worldNew.playSoundAtEntity(target, "sugiforest:sugi_portal", 0.5F, 1.15F);

						target.forceSpawn = false;
						target.getEntityData().setInteger("SugiForest:LastDim", dimOld);
					}

					entity.setDead();

					worldOld.resetUpdateEntityTick();
					worldNew.resetUpdateEntityTick();
				}
			}
			else
			{
				entity.timeUntilPortal = entity.getPortalCooldown();
			}
		}
	}

	@Override
	public PatternHelper func_181089_f(World world, BlockPos pos)
	{
		EnumFacing.Axis axis = EnumFacing.Axis.Z;
		Size size = new Size(world, pos, EnumFacing.Axis.X);
		LoadingCache<BlockPos, BlockWorldState> cache = BlockPattern.func_181627_a(world, true);

		if (!size.func_150860_b())
		{
			axis = EnumFacing.Axis.X;
			size = new Size(world, pos, EnumFacing.Axis.Z);
		}

		if (!size.func_150860_b())
		{
			return new PatternHelper(pos, EnumFacing.NORTH, EnumFacing.UP, cache, 1, 1, 1);
		}
		else
		{
			int[] aint = new int[EnumFacing.AxisDirection.values().length];
			EnumFacing facing = size.field_150866_c.rotateYCCW();
			BlockPos blockpos = size.field_150861_f.up(size.func_181100_a() - 1);

			for (EnumFacing.AxisDirection direction : EnumFacing.AxisDirection.values())
			{
				PatternHelper pattern = new PatternHelper(facing.getAxisDirection() == direction ? blockpos : blockpos.offset(size.field_150866_c, size.func_181101_b() - 1), EnumFacing.func_181076_a(direction, axis), EnumFacing.UP, cache, size.func_181101_b(), size.func_181100_a(), 1);

				for (int i = 0; i < size.func_181101_b(); ++i)
				{
					for (int j = 0; j < size.func_181100_a(); ++j)
					{
						BlockWorldState blockworldstate = pattern.translateOffset(i, j, 1);

						if (blockworldstate.getBlockState() != null && blockworldstate.getBlockState().getBlock().getMaterial() != Material.air)
						{
							++aint[direction.ordinal()];
						}
					}
				}
			}

			EnumFacing.AxisDirection var1 = EnumFacing.AxisDirection.POSITIVE;

			for (EnumFacing.AxisDirection direction : EnumFacing.AxisDirection.values())
			{
				if (aint[direction.ordinal()] < aint[var1.ordinal()])
				{
					var1 = direction;
				}
			}

			return new PatternHelper(facing.getAxisDirection() == var1 ? blockpos : blockpos.offset(size.field_150866_c, size.func_181101_b() - 1), EnumFacing.func_181076_a(var1, axis), EnumFacing.UP, cache, size.func_181101_b(), size.func_181100_a(), 1);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {}

	@SideOnly(Side.CLIENT)
	@Override
	public Item getItem(World worldIn, BlockPos pos)
	{
		return Item.getItemFromBlock(this);
	}

	public class Size
	{
		private final World world;
		private final EnumFacing.Axis axis;
		private final EnumFacing field_150866_c;
		private final EnumFacing field_150863_d;
		private int field_150864_e = 0;
		private BlockPos field_150861_f;
		private int field_150862_g;
		private int field_150868_h;

		public Size(World worldIn, BlockPos pos, EnumFacing.Axis axis)
		{
			this.world = worldIn;
			this.axis = axis;

			if (axis == EnumFacing.Axis.X)
			{
				this.field_150863_d = EnumFacing.EAST;
				this.field_150866_c = EnumFacing.WEST;
			}
			else
			{
				this.field_150863_d = EnumFacing.NORTH;
				this.field_150866_c = EnumFacing.SOUTH;
			}

			for (BlockPos blockpos1 = pos; pos.getY() > blockpos1.getY() - 21 && pos.getY() > 0 && func_150857_a(worldIn.getBlockState(pos.down()).getBlock()); pos = pos.down())
			{
				;
			}

			int i = func_180120_a(pos, field_150863_d) - 1;

			if (i >= 0)
			{
				this.field_150861_f = pos.offset(field_150863_d, i);
				this.field_150868_h = func_180120_a(field_150861_f, field_150866_c);

				if (field_150868_h < 2 || field_150868_h > 21)
				{
					this.field_150861_f = null;
					this.field_150868_h = 0;
				}
			}

			if (field_150861_f != null)
			{
				this.field_150862_g = func_150858_a();
			}
		}

		protected int func_180120_a(BlockPos pos, EnumFacing face)
		{
			int i;

			for (i = 0; i < 22; ++i)
			{
				BlockPos pos1 = pos.offset(face, i);

				if (!func_150857_a(world.getBlockState(pos1).getBlock()) || world.getBlockState(pos1.down()).getBlock() != SugiBlocks.sugi_log)
				{
					break;
				}
			}

			Block block = world.getBlockState(pos.offset(face, i)).getBlock();

			return block == SugiBlocks.sugi_log ? i : 0;
		}

		public int func_181100_a()
		{
			return field_150862_g;
		}

		public int func_181101_b()
		{
			return field_150868_h;
		}

		protected int func_150858_a()
		{
			int i;

			outside: for (field_150862_g = 0; field_150862_g < 21; ++field_150862_g)
			{
				for (i = 0; i < field_150868_h; ++i)
				{
					BlockPos pos = field_150861_f.offset(field_150866_c, i).up(field_150862_g);
					Block block = world.getBlockState(pos).getBlock();

					if (!func_150857_a(block))
					{
						break outside;
					}

					if (block == BlockSugiPortal.this)
					{
						++field_150864_e;
					}

					if (i == 0)
					{
						block = world.getBlockState(pos.offset(field_150863_d)).getBlock();

						if (block != SugiBlocks.sugi_log)
						{
							break outside;
						}
					}
					else if (i == field_150868_h - 1)
					{
						block = world.getBlockState(pos.offset(field_150866_c)).getBlock();

						if (block != SugiBlocks.sugi_log)
						{
							break outside;
						}
					}
				}
			}

			for (i = 0; i < field_150868_h; ++i)
			{
				if (world.getBlockState(field_150861_f.offset(field_150866_c, i).up(field_150862_g)).getBlock() != SugiBlocks.sugi_log)
				{
					field_150862_g = 0;
					break;
				}
			}

			if (field_150862_g <= 21 && field_150862_g >= 3)
			{
				return field_150862_g;
			}
			else
			{
				field_150861_f = null;
				field_150868_h = 0;
				field_150862_g = 0;

				return 0;
			}
		}

		protected boolean func_150857_a(Block block)
		{
			return block.getMaterial() == Material.air || block == BlockSugiPortal.this;
		}

		public boolean func_150860_b()
		{
			return field_150861_f != null && field_150868_h >= 2 && field_150868_h <= 21 && field_150862_g >= 3 && field_150862_g <= 21;
		}

		public void func_150859_c()
		{
			for (int i = 0; i < field_150868_h; ++i)
			{
				BlockPos pos = field_150861_f.offset(field_150866_c, i);

				for (int j = 0; j < field_150862_g; ++j)
				{
					world.setBlockState(pos.up(j), getDefaultState().withProperty(AXIS, axis), 2);
				}
			}
		}
	}
}