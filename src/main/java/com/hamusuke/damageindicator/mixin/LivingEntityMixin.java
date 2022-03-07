package com.hamusuke.damageindicator.mixin;

import com.hamusuke.damageindicator.invoker.LivingEntityInvoker;
import com.hamusuke.damageindicator.network.DamageIndicatorPacket;
import com.hamusuke.damageindicator.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityInvoker {
    protected int showImmuneCD;

    LivingEntityMixin(EntityType<?> type, World world) {
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
    private void damage(DamageSource p_21016_, float p_21017_, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof PlayerEntity) && !((Object) this instanceof ShulkerEntity) && !((Object) this instanceof WitherEntity)) {
            if (!this.level.isClientSide && !cir.getReturnValueZ() && this.canSendImmune(p_21017_)) {
                this.sendImmune();
            }
        }
    }

    @Override
    public void send(ITextComponent text, String source, boolean crit) {
        if (!this.level.isClientSide) {
            DamageIndicatorPacket damageIndicatorPacket = new DamageIndicatorPacket(this.getId(), text, source, crit);
            ((ServerWorld) this.level).players().forEach(serverPlayerEntity -> NetworkManager.sendToClient(damageIndicatorPacket, serverPlayerEntity));
        }
    }

    @Override
    public void sendImmune() {
        this.showImmuneCD = 10;
        this.send(new TranslationTextComponent("damageindicator.indicator.immune"), "immune", false);
    }

    @Override
    public boolean canSendImmune(float amount) {
        return this.getHealth() > 0.0F && this.showImmuneCD <= 0 && !((float) this.invulnerableTime > 10.0F && amount <= this.lastHurt);
    }
}
