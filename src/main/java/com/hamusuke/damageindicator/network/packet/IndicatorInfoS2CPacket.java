package com.hamusuke.damageindicator.network.packet;

import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class IndicatorInfoS2CPacket implements Packet {
    private final int entityId;
    private final Component text;
    private final String source;
    private final boolean crit;

    public IndicatorInfoS2CPacket(int entityId, Component text, String source, boolean crit) {
        this.entityId = entityId;
        this.text = text;
        this.source = source;
        this.crit = crit;
    }

    public IndicatorInfoS2CPacket(FriendlyByteBuf packetByteBuf) {
        this.entityId = packetByteBuf.readVarInt();
        this.text = packetByteBuf.readComponent();
        this.source = packetByteBuf.readUtf();
        this.crit = packetByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf packetByteBuf) {
        packetByteBuf.writeVarInt(this.entityId);
        packetByteBuf.writeComponent(this.text);
        packetByteBuf.writeUtf(this.source);
        packetByteBuf.writeBoolean(this.crit);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            DamageIndicatorClient.getInstance().addRenderer(this.entityId, this.text, this.source, this.crit);
            contextSupplier.get().setPacketHandled(true);
        });
    }
}
