package com.hamusuke.damageindicator.network;

import com.hamusuke.damageindicator.DamageIndicator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(DamageIndicator.MOD_ID, "damage_packet"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void init() {
        INSTANCE.registerMessage(0, DamageIndicatorPacket.class, DamageIndicatorPacket::write, DamageIndicatorPacket::new, DamageIndicatorPacket::handle);
    }

    public static void sendToClient(Packet packet, ServerPlayer serverPlayer) {
        INSTANCE.sendTo(packet, serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
