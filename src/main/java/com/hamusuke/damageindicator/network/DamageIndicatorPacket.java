package com.hamusuke.damageindicator.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class DamageIndicatorPacket implements IMessage {
    private double x;
    private double y;
    private double z;
    private String text = "";
    private String source = "generic";
    private boolean crit;

    public DamageIndicatorPacket() {
    }

    public DamageIndicatorPacket(double x, double y, double z, String text, String source, boolean crit) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.text = text;
        this.source = source;
        this.crit = crit;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.text = ByteBufUtils.readUTF8String(buf);
        this.source = ByteBufUtils.readUTF8String(buf);
        this.crit = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        ByteBufUtils.writeUTF8String(buf, this.text);
        ByteBufUtils.writeUTF8String(buf, this.source);
        buf.writeBoolean(this.crit);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
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
