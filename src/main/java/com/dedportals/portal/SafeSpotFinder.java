package com.dedportals.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Busca una posicion de llegada segura alrededor del destino configurado. */
public final class SafeSpotFinder {

    public static Optional<Vec3> find(ServerLevel level, double x, double y, double z, int radius) {
        BlockPos center = BlockPos.containing(x, y, z);
        int vMax = radius + 3;
        for (int r = 0; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    for (int k = 0; k <= vMax * 2; k++) {
                        int dy = (k % 2 == 0) ? k / 2 : -(k + 1) / 2;
                        BlockPos p = center.offset(dx, dy, dz);
                        if (isSafe(level, p)) {
                            double px = (r == 0 && dx == 0 && dz == 0) ? x : p.getX() + 0.5;
                            double pz = (r == 0 && dx == 0 && dz == 0) ? z : p.getZ() + 0.5;
                            return Optional.of(new Vec3(px, p.getY(), pz));
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static boolean isSafe(ServerLevel level, BlockPos feet) {
        if (feet.getY() <= level.getMinBuildHeight() + 1 || feet.getY() >= level.getMaxBuildHeight() - 3) return false;
        if (!level.getWorldBorder().isWithinBounds(feet)) return false;

        BlockPos head = feet.above();
        BlockPos floor = feet.below();
        BlockState fs = level.getBlockState(feet);
        BlockState hs = level.getBlockState(head);
        BlockState gs = level.getBlockState(floor);

        if (!fs.getFluidState().isEmpty() || !hs.getFluidState().isEmpty()) return false;
        if (fs.is(BlockTags.FIRE) || hs.is(BlockTags.FIRE) || fs.is(Blocks.CACTUS) || hs.is(Blocks.CACTUS)
                || fs.is(Blocks.SWEET_BERRY_BUSH) || fs.is(Blocks.WITHER_ROSE) || fs.is(Blocks.POWDER_SNOW)) return false;
        if (!gs.getFluidState().isEmpty()) return false;
        if (gs.is(Blocks.MAGMA_BLOCK) || gs.is(Blocks.CACTUS) || gs.is(BlockTags.FIRE) || gs.is(BlockTags.CAMPFIRES)
                || gs.is(Blocks.POWDER_SNOW)) return false;
        if (!gs.isFaceSturdy(level, floor, Direction.UP)) return false;

        AABB box = new AABB(feet.getX() + 0.2, feet.getY(), feet.getZ() + 0.2,
                feet.getX() + 0.8, feet.getY() + 1.9, feet.getZ() + 0.8);
        return level.noCollision(box);
    }

    private SafeSpotFinder() {}
}
