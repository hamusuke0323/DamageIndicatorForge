package com.hamusuke.damageindicator.network;

import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.function.Supplier;

public class DamageIndicatorPacket implements Packet {
    private final double x;
    private final double y;
    private final double z;
    private final ITextComponent text;
    private final String source;
    private final boolean crit;

    public DamageIndicatorPacket(double x, double y, double z, ITextComponent text, String source, boolean crit) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.text = text;
        this.source = source;
        this.crit = crit;
    }

    public DamageIndicatorPacket(PacketBuffer packetByteBuf) {
        this.x = packetByteBuf.readDouble();
        this.y = packetByteBuf.readDouble();
        this.z = packetByteBuf.readDouble();
        this.text = packetByteBuf.readComponent();
        this.source = packetByteBuf.readUtf();
        this.crit = packetByteBuf.readBoolean();
    }

    @Override
    public void write(PacketBuffer packetByteBuf) {
        packetByteBuf.writeDouble(this.x);
        packetByteBuf.writeDouble(this.y);
        packetByteBuf.writeDouble(this.z);
        packetByteBuf.writeComponent(this.text);
        packetByteBuf.writeUtf(this.source);
        packetByteBuf.writeBoolean(this.crit);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            DamageIndicatorClient.getInstance().addRenderer(this.x, this.y, this.z, this.text, this.source, this.crit);
            mutableBoolean.setTrue();
        });
        contextSupplier.get().setPacketHandled(mutableBoolean.booleanValue());
    }
}
