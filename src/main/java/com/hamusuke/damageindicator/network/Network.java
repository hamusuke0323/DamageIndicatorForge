package com.hamusuke.damageindicator.network;

import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.network.packet.IndicatorInfoS2CPacket;
import com.hamusuke.damageindicator.network.packet.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Network {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(DamageIndicator.MOD_ID, "damage_packet"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    static {
        INSTANCE.registerMessage(0, IndicatorInfoS2CPacket.class, IndicatorInfoS2CPacket::write, IndicatorInfoS2CPacket::new, IndicatorInfoS2CPacket::handle);
    }

    public static void load() {
    }

    public static void send2C(Packet packet, ServerPlayer serverPlayer) {
        INSTANCE.sendTo(packet, serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
