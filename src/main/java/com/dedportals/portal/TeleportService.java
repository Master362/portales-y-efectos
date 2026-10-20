package com.dedportals.portal;

import com.dedportals.DedPortalsMod;
import com.dedportals.config.DedConfig;
import com.dedportals.network.ModNetwork;
import com.dedportals.network.PortalEffectPacket;
import com.dedportals.registry.ModParticles;
import com.dedportals.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Logica de teletransporte 100% del lado servidor. */
@Mod.EventBusSubscriber(modid = DedPortalsMod.MODID)
public final class TeleportService {
    private record Pending(String portal, int executeAt, boolean effects) {}

    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final Map<UUID, Integer> COOLDOWN = new HashMap<>();

    public static void tryEnter(ServerPlayer player, PortalData d) {
        if (!d.enabled || !d.hasDest) return;
        MinecraftServer server = player.server;
        int now = server.getTickCount();
        UUID id = player.getUUID();
        if (PENDING.containsKey(id)) return;
        Integer cd = COOLDOWN.get(id);
        if (cd != null && now < cd) return;

        int delay = d.delay >= 0 ? d.delay : DedConfig.COMMON.teleportDelayTicks.get();
        PENDING.put(id, new Pending(d.name, now + delay, d.effects));
        if (d.effects) {
            ModNetwork.sendTo(player, new PortalEffectPacket(PortalEffectPacket.ENTER, Math.max(delay, 1)));
        }
        player.level().playSound(null, player.blockPosition(), ModSounds.PORTAL_ENTER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) return;
        MinecraftServer server = event.getServer();
        int now = server.getTickCount();
        List<UUID> due = new ArrayList<>();
        for (Map.Entry<UUID, Pending> e : PENDING.entrySet()) {
            if (now >= e.getValue().executeAt()) due.add(e.getKey());
        }
        for (UUID id : due) {
            Pending p = PENDING.remove(id);
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null && player.isAlive()) execute(server, player, p);
        }
        Iterator<Map.Entry<UUID, Integer>> it = COOLDOWN.entrySet().iterator();
        while (it.hasNext()) {
            if (now >= it.next().getValue() + 200) it.remove();
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
        COOLDOWN.remove(event.getEntity().getUUID());
    }

    private static void execute(MinecraftServer server, ServerPlayer player, Pending p) {
        PortalData d = PortalManager.get(server).get(p.portal());
        if (d == null || !d.enabled || !d.hasDest) {
            fail(server, player, p, "El portal ya no esta disponible.");
            return;
        }
        ServerLevel dest = server.getLevel(ResourceKey.create(Registries.DIMENSION, d.destDimension));
        if (dest == null) {
            fail(server, player, p, "La dimension de destino no existe: " + d.destDimension);
            return;
        }
        Optional<Vec3> spot = SafeSpotFinder.find(dest, d.dx, d.dy, d.dz, DedConfig.COMMON.safeSearchRadius.get());
        if (spot.isEmpty()) {
            fail(server, player, p, "No se encontro una ubicacion de llegada segura.");
            return;
        }
        Vec3 v = spot.get();
        player.stopRiding();
        player.teleportTo(dest, v.x, v.y, v.z, d.dyaw, 0.0F);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        COOLDOWN.put(player.getUUID(), server.getTickCount() + DedConfig.COMMON.cooldownTicks.get());

        dest.playSound(null, BlockPos.containing(v), ModSounds.PORTAL_ARRIVE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        dest.sendParticles(ModParticles.GREEN_SPARK.get(), v.x, v.y + 1.0, v.z, 40, 0.5, 0.8, 0.5, 0.1);
        if (p.effects()) {
            ModNetwork.sendTo(player, new PortalEffectPacket(PortalEffectPacket.ARRIVE, 0));
        }
    }

    private static void fail(MinecraftServer server, ServerPlayer player, Pending p, String msg) {
        COOLDOWN.put(player.getUUID(), server.getTickCount() + 40);
        player.displayClientMessage(Component.literal(msg), true);
        if (p.effects()) {
            ModNetwork.sendTo(player, new PortalEffectPacket(PortalEffectPacket.ARRIVE, 0));
        }
    }

    private TeleportService() {}
}
