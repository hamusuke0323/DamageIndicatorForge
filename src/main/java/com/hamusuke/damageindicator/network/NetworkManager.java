package com.hamusuke.damageindicator.network;

import com.hamusuke.damageindicator.DamageIndicator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkManager {
    private static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(DamageIndicator.MOD_ID + ".damage_packet");

    public static void init() {
        INSTANCE.registerMessage(DamageIndicatorPacketHandler.class, DamageIndicatorPacket.class, 0, Side.CLIENT);
    }

    public static void sendToClient(IMessage packet, EntityPlayerMP serverPlayer) {
        INSTANCE.sendTo(packet, serverPlayer);
    }
}
