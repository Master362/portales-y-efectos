package com.dedportals.registry;

import com.dedportals.DedPortalsMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, DedPortalsMod.MODID);

    public static final RegistryObject<SimpleParticleType> GREEN_SPARK =
            PARTICLES.register("green_spark", () -> new SimpleParticleType(false));

    private ModParticles() {}
}
