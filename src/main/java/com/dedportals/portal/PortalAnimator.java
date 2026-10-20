package com.dedportals.portal;

import com.dedportals.DedPortalsMod;
import com.dedportals.registry.ModParticles;
import com.dedportals.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Animacion de apertura/cierre: el interior se llena (o vacia) fila a fila con particulas y sonido. */
@Mod.EventBusSubscriber(modid = DedPortalsMod.MODID)
public final class PortalAnimator {
    private static final int TICKS_PER_ROW = 3;
    private static final List<Task> TASKS = new ArrayList<>();

    private static final class Task {
        final ServerLevel level;
        final PortalData data;
        final boolean open;
        int tick = 0;

        Task(ServerLevel level, PortalData data, boolean open) {
            this.level = level;
            this.data = data;
            this.open = open;
        }
    }

    public static void start(ServerLevel level, PortalData d, boolean open) {
        cancel(d);
        TASKS.add(new Task(level, d, open));
        Vec3 c = center(d);
        level.playSound(null, BlockPos.containing(c), open ? ModSounds.PORTAL_OPEN.get() : ModSounds.PORTAL_CLOSE.get(),
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void cancel(PortalData d) {
        TASKS.removeIf(t -> t.data == d);
    }

    private static Vec3 center(PortalData d) {
        BlockPos a = d.pos(0, 0);
        BlockPos b = d.pos(PortalData.W - 1, PortalData.H - 1);
        return new Vec3((a.getX() + b.getX()) / 2.0 + 0.5, (a.getY() + b.getY()) / 2.0 + 0.5, (a.getZ() + b.getZ()) / 2.0 + 0.5);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TASKS.isEmpty()) return;
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            t.tick++;
            if (t.tick % TICKS_PER_ROW != 0) continue;
            int step = t.tick / TICKS_PER_ROW - 1;
            if (step >= PortalData.H) {
                it.remove();
                continue;
            }
            int j = t.open ? step : PortalData.H - 1 - step;
            for (int i = 0; i < PortalData.W; i++) {
                PortalBuilder.setCell(t.level, t.data, i, j, t.open);
                BlockPos p = t.data.pos(i, j);
                t.level.sendParticles(ModParticles.GREEN_SPARK.get(), p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                        12, 0.35, 0.35, 0.35, 0.05);
            }
        }
    }

    private PortalAnimator() {}
}
