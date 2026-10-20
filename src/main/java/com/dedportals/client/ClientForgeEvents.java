package com.dedportals.client;

import com.dedportals.DedPortalsMod;
import com.dedportals.config.DedConfig;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DedPortalsMod.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        ClientEffects.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov event) {
        if (!DedConfig.CLIENT.cameraEffects.get()) return;
        float i = ClientEffects.intensity();
        if (i > 0f) {
            event.setFOV(event.getFOV() * (1.0 + 0.45 * i));
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!DedConfig.CLIENT.cameraEffects.get()) return;
        float i = ClientEffects.intensity();
        if (i > 0f) {
            double t = Util.getMillis() / 1000.0;
            event.setRoll(event.getRoll() + (float) (Math.sin(t * 7.0) * 5.0 * i));
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientEffects.reset();
    }

    private ClientForgeEvents() {}
}
