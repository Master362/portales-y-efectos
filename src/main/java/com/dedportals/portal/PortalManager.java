package com.dedportals.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Almacen persistente (mundo) de todos los portales. */
public class PortalManager extends SavedData {
    private static final String ID = "dedportals_portals";
    private final Map<String, PortalData> portals = new LinkedHashMap<>();

    public static PortalManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PortalManager::load, PortalManager::new, ID);
    }

    public static PortalManager load(CompoundTag tag) {
        PortalManager m = new PortalManager();
        ListTag list = tag.getList("portals", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            PortalData d = PortalData.load(list.getCompound(i));
            m.portals.put(d.name, d);
        }
        return m;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (PortalData d : portals.values()) list.add(d.save());
        tag.put("portals", list);
        return tag;
    }

    public PortalData get(String name) {
        return portals.get(name);
    }

    public void add(PortalData d) {
        portals.put(d.name, d);
        setDirty();
    }

    public void remove(String name) {
        portals.remove(name);
        setDirty();
    }

    public Collection<PortalData> all() {
        return portals.values();
    }

    public List<String> names() {
        return new ArrayList<>(portals.keySet());
    }

    public PortalData findAt(ResourceLocation dim, BlockPos pos) {
        for (PortalData d : portals.values()) {
            if (d.dimension.equals(dim) && d.contains(pos)) return d;
        }
        return null;
    }
}
