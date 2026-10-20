package com.dedportals.client;

import com.dedportals.DedPortalsMod;
import com.dedportals.registry.ModParticles;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DedPortalsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(),
                            new ResourceLocation(DedPortalsMod.MODID, "portal_warp"),
                            DefaultVertexFormat.POSITION_TEX),
                    inst -> ClientEffects.shader = inst);
        } catch (Exception e) {
            DedPortalsMod.LOGGER.error("No se pudo cargar el shader del portal; se usara el efecto alternativo.", e);
            ClientEffects.shader = null;
        }
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.GREEN_SPARK.get(), GreenSparkParticle.Provider::new);
    }

    private ClientModEvents() {}
}
