package com.hamusuke.damageindicator.proxy;

import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import com.hamusuke.damageindicator.config.ClientConfig;
import com.hamusuke.damageindicator.network.DamageIndicatorPacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new DamageIndicatorClient());
    }

    @Override
    public void onConfigChanged(Configuration config) {
        super.onConfigChanged(config);
        ClientConfig.sync(config);
        DamageIndicatorClient.getInstance().syncIndicatorColor();
    }

    @Override
    public IMessage onMessage(DamageIndicatorPacket packet, MessageContext context) {
        DamageIndicatorClient.getInstance().addRenderer(packet.getEntityId(), packet.getText(), packet.getSource(), packet.isCrit());
        return null;
    }
}
