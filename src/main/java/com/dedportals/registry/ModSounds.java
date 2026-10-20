package com.dedportals.registry;

import com.dedportals.DedPortalsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, DedPortalsMod.MODID);

    public static final RegistryObject<SoundEvent> PORTAL_AMBIENT = reg("portal.ambient");
    public static final RegistryObject<SoundEvent> PORTAL_ENTER = reg("portal.enter");
    public static final RegistryObject<SoundEvent> PORTAL_ARRIVE = reg("portal.arrive");
    public static final RegistryObject<SoundEvent> PORTAL_OPEN = reg("portal.open");
    public static final RegistryObject<SoundEvent> PORTAL_CLOSE = reg("portal.close");

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(DedPortalsMod.MODID, name)));
    }

    private ModSounds() {}
}
