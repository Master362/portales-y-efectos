package com.dedportals.block;

import com.dedportals.portal.PortalData;
import com.dedportals.portal.PortalManager;
import com.dedportals.portal.TeleportService;
import com.dedportals.registry.ModParticles;
import com.dedportals.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape X_SHAPE = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape Z_SHAPE = Block.box(6, 0, 0, 10, 16, 16);

    public PortalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(AXIS) == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction dir) {
        return adjacent.is(this);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) return;
        PortalData data = PortalManager.get(player.server).findAt(level.dimension().location(), pos);
        if (data != null) {
            TeleportService.tryEnter(player, data);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        boolean x = state.getValue(AXIS) == Direction.Axis.X;
        for (int i = 0; i < 2; i++) {
            double px = pos.getX() + (x ? rand.nextDouble() : 0.35 + rand.nextDouble() * 0.3);
            double py = pos.getY() + rand.nextDouble();
            double pz = pos.getZ() + (x ? 0.35 + rand.nextDouble() * 0.3 : rand.nextDouble());
            level.addParticle(ModParticles.GREEN_SPARK.get(), px, py, pz,
                    (rand.nextDouble() - 0.5) * 0.04, rand.nextDouble() * 0.05, (rand.nextDouble() - 0.5) * 0.04);
        }
        if (rand.nextInt(120) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    ModSounds.PORTAL_AMBIENT.get(), SoundSource.BLOCKS, 0.6F, 0.9F + rand.nextFloat() * 0.2F, false);
        }
    }
}
