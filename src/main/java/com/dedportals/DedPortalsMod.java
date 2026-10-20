package com.dedportals;

import com.dedportals.config.DedConfig;
import com.dedportals.network.ModNetwork;
import com.dedportals.registry.ModBlocks;
import com.dedportals.registry.ModItems;
import com.dedportals.registry.ModParticles;
import com.dedportals.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(DedPortalsMod.MODID)
public class DedPortalsMod {
    public static final String MODID = "dedportals";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DedPortalsMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModSounds.SOUNDS.register(bus);
        ModParticles.PARTICLES.register(bus);
        bus.addListener(this::commonSetup);
        bus.addListener(ModItems::addCreative);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DedConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DedConfig.CLIENT_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
