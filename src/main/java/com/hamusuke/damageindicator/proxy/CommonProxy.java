package com.hamusuke.damageindicator.proxy;

import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.network.DamageIndicatorPacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CommonProxy {
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(new DamageIndicator());
    }

    public IMessage onMessage(DamageIndicatorPacket packet, MessageContext context) {
        return null;
    }
}
