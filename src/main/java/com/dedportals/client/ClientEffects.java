package com.dedportals.client;

import com.dedportals.DedPortalsMod;
import com.dedportals.config.DedConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Estado y render del efecto de entrada/llegada del portal (solo cliente). */
public final class ClientEffects {
    private enum State { IDLE, ENTER, HOLD, ARRIVE }

    /** Asignado desde RegisterShadersEvent; null si el shader no pudo cargarse. */
    public static volatile ShaderInstance shader;
    private static boolean shaderBroken = false;
    private static RenderTarget sceneCopy;

    private static State state = State.IDLE;
    private static long stateStart;
    private static long enterMs = 700;
    private static final long HOLD_TIMEOUT_MS = 4000;

    private ClientEffects() {}

    public static void onPacket(byte phase, int ticks) {
        if (!DedConfig.CLIENT.enableEffects.get()) return;
        long now = Util.getMillis();
        if (phase == 0) {
            state = State.ENTER;
            stateStart = now;
            enterMs = Math.max(ticks * 50L, 200L);
        } else if (state != State.IDLE) {
            state = State.ARRIVE;
            stateStart = now;
        }
    }

    public static void reset() {
        state = State.IDLE;
    }

    private static float smooth(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    /** Intensidad actual 0..1 (avanza la maquina de estados). */
    public static float intensity() {
        if (state == State.IDLE) return 0f;
        long now = Util.getMillis();
        float base;
        switch (state) {
            case ENTER -> {
                float t = (now - stateStart) / (float) enterMs;
                if (t >= 1f) {
                    state = State.HOLD;
                    stateStart = now;
                    base = 1f;
                } else {
                    base = smooth(t);
                }
            }
            case HOLD -> {
                if (now - stateStart > HOLD_TIMEOUT_MS) { // seguridad: nunca dejar la pantalla bloqueada
                    state = State.ARRIVE;
                    stateStart = now;
                }
                base = 1f;
            }
            case ARRIVE -> {
                float fade = DedConfig.CLIENT.fadeOutTicks.get() * 50f;
                float t = (now - stateStart) / fade;
                if (t >= 1f) {
                    state = State.IDLE;
                    base = 0f;
                } else {
                    float inv = 1f - t;
                    base = inv * inv; // salida suave
                }
            }
            default -> base = 0f;
        }
        return base * DedConfig.CLIENT.intensity.get().floatValue();
    }

    public static boolean isArriving() {
        return state == State.ARRIVE;
    }

    public static void render(GuiGraphics g) {
        float i = intensity();
        if (i <= 0.001f) return;
        if (DedConfig.CLIENT.useShader.get() && shader != null && !shaderBroken) {
            try {
                renderShader(i);
                return;
            } catch (Throwable t) {
                shaderBroken = true;
                DedPortalsMod.LOGGER.error("Fallo el shader del portal; se usa el efecto alternativo.", t);
            }
        }
        renderFallback(g, i);
    }

    private static void renderShader(float intensity) {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        int w = main.width;
        int h = main.height;

        if (sceneCopy == null) {
            sceneCopy = new TextureTarget(w, h, false, Minecraft.ON_OSX);
        } else if (sceneCopy.width != w || sceneCopy.height != h) {
            sceneCopy.resize(w, h, Minecraft.ON_OSX);
        }
        sceneCopy.setFilterMode(GL11.GL_LINEAR);

        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, sceneCopy.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        main.bindWrite(true);

        ShaderInstance s = shader;
        s.setSampler("Scene", sceneCopy.getColorTextureId());
        s.safeGetUniform("Time").set((Util.getMillis() % 100000L) / 1000f);
        s.safeGetUniform("Intensity").set(intensity);
        s.safeGetUniform("Aspect").set(w / (float) h);
        s.safeGetUniform("Phase").set(isArriving() ? 1f : 0f);

        RenderSystem.setShader(() -> s);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        b.vertex(-1.0, -1.0, 0.0).uv(0f, 0f).endVertex();
        b.vertex(1.0, -1.0, 0.0).uv(1f, 0f).endVertex();
        b.vertex(1.0, 1.0, 0.0).uv(1f, 1f).endVertex();
        b.vertex(-1.0, 1.0, 0.0).uv(0f, 1f).endVertex();
        BufferUploader.drawWithShader(b.end());
        RenderSystem.enableDepthTest();
    }

    /** Efecto alternativo sin shader: velo verde pulsante con vineta. */
    private static void renderFallback(GuiGraphics g, float i) {
        int w = g.guiWidth();
        int h = g.guiHeight();
        float pulse = 0.85f + 0.15f * (float) Math.sin(Util.getMillis() / 90.0);
        int a = Math.min(255, (int) (i * 235 * pulse));
        int edge = Math.min(255, (int) (i * 255));
        g.fill(0, 0, w, h, (a << 24) | 0x1CD65A);
        g.fillGradient(0, 0, w, h / 3, (edge << 24) | 0x004010, 0x00004010);
        g.fillGradient(0, h * 2 / 3, w, h, 0x00004010, (edge << 24) | 0x004010);
        int core = Math.min(255, (int) (Math.pow(i, 3) * 200));
        g.fill(w / 3, h / 3, w * 2 / 3, h * 2 / 3, (core << 24) | 0xD8FFE4);
    }
}
