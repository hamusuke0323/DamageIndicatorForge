package com.hamusuke.damageindicator.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface Packet {
    void write(FriendlyByteBuf friendlyByteBuf);

    void handle(Supplier<NetworkEvent.Context> contextSupplier);
}
