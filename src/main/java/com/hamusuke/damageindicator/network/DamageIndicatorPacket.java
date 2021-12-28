package com.hamusuke.damageindicator.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class DamageIndicatorPacket implements IMessage {
    private int entityId;
    private String text = "";
    private String source = "generic";
    private boolean crit;

    public DamageIndicatorPacket() {
    }

    public DamageIndicatorPacket(int entityId, String text, String source, boolean crit) {
        this.entityId = entityId;
        this.text = text;
        this.source = source;
        this.crit = crit;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.text = ByteBufUtils.readUTF8String(buf);
        this.source = ByteBufUtils.readUTF8String(buf);
        this.crit = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.text);
        ByteBufUtils.writeUTF8String(buf, this.source);
        buf.writeBoolean(this.crit);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getText() {
        return this.text;
    }

    public String getSource() {
        return this.source;
    }

    public boolean isCrit() {
        return this.crit;
    }
}
