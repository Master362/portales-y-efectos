package com.dedportals.network;

import com.dedportals.client.ClientEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: inicia la absorcion (ENTER) o la llegada (ARRIVE). */
public record PortalEffectPacket(byte phase, int ticks) {
    public static final byte ENTER = 0;
    public static final byte ARRIVE = 1;

    public static void encode(PortalEffectPacket m, FriendlyByteBuf buf) {
        buf.writeByte(m.phase);
        buf.writeVarInt(m.ticks);
    }

    public static PortalEffectPacket decode(FriendlyByteBuf buf) {
        return new PortalEffectPacket(buf.readByte(), buf.readVarInt());
    }

    public static void handle(PortalEffectPacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientEffects.onPacket(m.phase(), m.ticks())));
        ctx.get().setPacketHandled(true);
    }
}
