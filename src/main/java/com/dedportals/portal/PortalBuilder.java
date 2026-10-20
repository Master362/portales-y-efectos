package com.dedportals.portal;

import com.dedportals.block.PortalBlock;
import com.dedportals.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Construccion instantanea de marco e interior. */
public final class PortalBuilder {
    public static BlockState portalState(PortalData d) {
        return ModBlocks.PORTAL.get().defaultBlockState().setValue(PortalBlock.AXIS, d.axis);
    }

    private static boolean isInner(int i, int j) {
        return i >= 0 && i < PortalData.W && j >= 0 && j < PortalData.H;
    }

    /** Coloca el marco; el interior queda con el portal si esta activo, o aire si no. */
    public static void buildFrame(ServerLevel level, PortalData d) {
        BlockState frame = ModBlocks.PORTAL_FRAME.get().defaultBlockState();
        for (int i = -1; i <= PortalData.W; i++) {
            for (int j = -1; j <= PortalData.H; j++) {
                BlockPos p = d.pos(i, j);
                if (!isInner(i, j)) {
                    level.setBlock(p, frame, 3);
                } else {
                    level.setBlock(p, d.enabled ? portalState(d) : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    public static void setInterior(ServerLevel level, PortalData d, boolean on) {
        for (int i = 0; i < PortalData.W; i++) {
            for (int j = 0; j < PortalData.H; j++) {
                setCell(level, d, i, j, on);
            }
        }
    }

    public static void setCell(ServerLevel level, PortalData d, int i, int j, boolean on) {
        BlockPos p = d.pos(i, j);
        BlockState cur = level.getBlockState(p);
        if (on) {
            level.setBlock(p, portalState(d), 3);
        } else if (cur.is(ModBlocks.PORTAL.get())) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    /** Elimina marco e interior (solo bloques del mod). */
    public static void removeAll(ServerLevel level, PortalData d) {
        for (int i = -1; i <= PortalData.W; i++) {
            for (int j = -1; j <= PortalData.H; j++) {
                BlockPos p = d.pos(i, j);
                BlockState s = level.getBlockState(p);
                if (s.is(ModBlocks.PORTAL.get()) || s.is(ModBlocks.PORTAL_FRAME.get())) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private PortalBuilder() {}
}
