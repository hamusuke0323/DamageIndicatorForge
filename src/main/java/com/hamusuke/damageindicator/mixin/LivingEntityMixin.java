package com.hamusuke.damageindicator.mixin;

import com.hamusuke.damageindicator.invoker.LivingEntityInvoker;
import com.hamusuke.damageindicator.network.Network;
import com.hamusuke.damageindicator.network.packet.IndicatorInfoS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityInvoker {
    protected int showImmuneCD;

    LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    public abstract float getHealth();

    @Shadow
    protected float lastHurt;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (this.showImmuneCD > 0) {
            this.showImmuneCD--;
        }
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void hurt(DamageSource p_21016_, float p_21017_, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof Player) && !((Object) this instanceof Shulker) && !((Object) this instanceof WitherBoss)) {
            if (!this.level.isClientSide && !cir.getReturnValueZ() && this.canSendImmune(p_21017_)) {
                this.sendImmune();
            }
        }
    }

    @Override
    public void sendDMGPacketToAll(Component text, String source, boolean crit) {
        if (!this.level.isClientSide) {
            IndicatorInfoS2CPacket packet = new IndicatorInfoS2CPacket(this.getId(), text, source, crit);
            ((ServerLevel) this.level).players().forEach(serverPlayerEntity -> Network.send2C(packet, serverPlayerEntity));
        }
    }

    @Override
    public void sendImmune() {
        this.showImmuneCD = 10;
        this.sendDMGPacketToAll(Component.translatable("damageindicator.indicator.immune"), "immune", false);
    }

    @Override
    public boolean canSendImmune(float amount) {
        return this.getHealth() > 0.0F && this.showImmuneCD <= 0 && !((float) this.invulnerableTime > 10.0F && amount <= this.lastHurt);
    }
}
