package com.hamusuke.damageindicator.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface Packet {
    void write(PacketBuffer friendlyByteBuf);

    void handle(Supplier<NetworkEvent.Context> contextSupplier);
}
