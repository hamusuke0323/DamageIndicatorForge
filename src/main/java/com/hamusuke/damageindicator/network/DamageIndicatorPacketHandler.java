package com.hamusuke.damageindicator.network;

import com.hamusuke.damageindicator.DamageIndicator;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DamageIndicatorPacketHandler implements IMessageHandler<DamageIndicatorPacket, IMessage> {
    @Override
    public IMessage onMessage(DamageIndicatorPacket message, MessageContext ctx) {
        return DamageIndicator.PROXY.onMessage(message, ctx);
    }
}
