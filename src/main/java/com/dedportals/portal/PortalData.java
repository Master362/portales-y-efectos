package com.dedportals.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Definicion persistente de un portal. Interior de W x H bloques; marco de (W+2) x (H+2). */
public final class PortalData {
    public static final int W = 2;
    public static final int H = 3;

    public final String name;
    public ResourceLocation dimension;
    public BlockPos origin;           // bloque interior inferior de menor coordenada
    public Direction.Axis axis;       // eje a lo largo del cual se extiende el portal (X o Z)
    public Direction facing;          // lado frontal (hacia donde se sale)
    public boolean enabled = true;
    public boolean hasDest = false;
    public ResourceLocation destDimension = Level.OVERWORLD.location();
    public double dx, dy, dz;
    public float dyaw;
    public boolean effects = true;
    public int delay = -1;            // -1 = usar config

    public PortalData(String name, ResourceLocation dimension, BlockPos origin, Direction.Axis axis, Direction facing) {
        this.name = name;
        this.dimension = dimension;
        this.origin = origin;
        this.axis = axis;
        this.facing = facing;
    }

    public BlockPos pos(int i, int j) {
        return axis == Direction.Axis.X ? origin.offset(i, j, 0) : origin.offset(0, j, i);
    }

    public boolean contains(BlockPos p) {
        int j = p.getY() - origin.getY();
        if (j < 0 || j >= H) return false;
        if (axis == Direction.Axis.X) {
            int i = p.getX() - origin.getX();
            return p.getZ() == origin.getZ() && i >= 0 && i < W;
        }
        int i = p.getZ() - origin.getZ();
        return p.getX() == origin.getX() && i >= 0 && i < W;
    }

    /** Punto de salida frente al portal (para vincular otros portales hacia este). */
    public Vec3 exitPos() {
        double cx = axis == Direction.Axis.X ? origin.getX() + 1.0 : origin.getX() + 0.5;
        double cz = axis == Direction.Axis.X ? origin.getZ() + 0.5 : origin.getZ() + 1.0;
        return new Vec3(cx + facing.getStepX() * 1.5, origin.getY(), cz + facing.getStepZ() * 1.5);
    }

    public void setDestination(ResourceLocation dim, double x, double y, double z, float yaw) {
        this.hasDest = true;
        this.destDimension = dim;
        this.dx = x;
        this.dy = y;
        this.dz = z;
        this.dyaw = yaw;
    }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putString("name", name);
        t.putString("dim", dimension.toString());
        t.putLong("origin", origin.asLong());
        t.putString("axis", axis.getName());
        t.putString("facing", facing.getName());
        t.putBoolean("enabled", enabled);
        t.putBoolean("hasDest", hasDest);
        t.putString("destDim", destDimension.toString());
        t.putDouble("dx", dx);
        t.putDouble("dy", dy);
        t.putDouble("dz", dz);
        t.putFloat("dyaw", dyaw);
        t.putBoolean("effects", effects);
        t.putInt("delay", delay);
        return t;
    }

    public static PortalData load(CompoundTag t) {
        PortalData d = new PortalData(
                t.getString("name"),
                new ResourceLocation(t.getString("dim")),
                BlockPos.of(t.getLong("origin")),
                Direction.Axis.byName(t.getString("axis")),
                Direction.byName(t.getString("facing")));
        d.enabled = t.getBoolean("enabled");
        d.hasDest = t.getBoolean("hasDest");
        d.destDimension = new ResourceLocation(t.getString("destDim"));
        d.dx = t.getDouble("dx");
        d.dy = t.getDouble("dy");
        d.dz = t.getDouble("dz");
        d.dyaw = t.getFloat("dyaw");
        d.effects = !t.contains("effects") || t.getBoolean("effects");
        d.delay = t.contains("delay") ? t.getInt("delay") : -1;
        return d;
    }
}
