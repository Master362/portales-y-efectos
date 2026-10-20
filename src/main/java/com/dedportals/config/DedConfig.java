package com.dedportals.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class DedConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Common, ForgeConfigSpec> c = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = c.getLeft();
        COMMON_SPEC = c.getRight();
        Pair<Client, ForgeConfigSpec> k = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = k.getLeft();
        CLIENT_SPEC = k.getRight();
    }

    public static final class Common {
        public final ForgeConfigSpec.IntValue teleportDelayTicks;
        public final ForgeConfigSpec.IntValue cooldownTicks;
        public final ForgeConfigSpec.IntValue safeSearchRadius;

        Common(ForgeConfigSpec.Builder b) {
            b.push("teleport");
            teleportDelayTicks = b.comment("Ticks entre tocar el portal y ser teletransportado (duracion de la absorcion).")
                    .defineInRange("teleportDelayTicks", 14, 0, 100);
            cooldownTicks = b.comment("Ticks de enfriamiento tras teletransportarse.")
                    .defineInRange("cooldownTicks", 60, 0, 1200);
            safeSearchRadius = b.comment("Radio (bloques) para buscar una posicion segura de llegada.")
                    .defineInRange("safeSearchRadius", 6, 0, 32);
            b.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue enableEffects;
        public final ForgeConfigSpec.BooleanValue useShader;
        public final ForgeConfigSpec.BooleanValue cameraEffects;
        public final ForgeConfigSpec.DoubleValue intensity;
        public final ForgeConfigSpec.IntValue fadeOutTicks;

        Client(ForgeConfigSpec.Builder b) {
            b.push("effects");
            enableEffects = b.comment("Activa el efecto visual al cruzar un portal.").define("enableEffects", true);
            useShader = b.comment("Usa el shader personalizado. Si es false (o falla) se usa un efecto alternativo.")
                    .define("useShader", true);
            cameraEffects = b.comment("Distorsion de FOV y giro de camara durante el efecto.").define("cameraEffects", true);
            intensity = b.comment("Intensidad global del efecto.").defineInRange("intensity", 1.0, 0.1, 1.0);
            fadeOutTicks = b.comment("Ticks que tarda en recuperarse la vision al llegar.").defineInRange("fadeOutTicks", 45, 10, 200);
            b.pop();
        }
    }

    private DedConfig() {}
}
