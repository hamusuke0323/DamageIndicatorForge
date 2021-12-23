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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityInvoker {
    protected boolean isCritical;

    LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean isDeadOrDying();

    @Inject(method = "hurt", at = @At("RETURN"))
    private void damage(DamageSource p_21016_, float p_21017_, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof PlayerEntity) && !((Object) this instanceof ShulkerEntity) && !((Object) this instanceof WitherEntity)) {
            if (!this.level.isClientSide && !cir.getReturnValue() && !this.isDeadOrDying()) {
                this.sendImmune();
            }
        }
    }

    @Override
    public void send(ITextComponent text, String source, boolean crit) {
        if (!this.level.isClientSide) {
            DamageIndicatorPacket damageIndicatorPacket = new DamageIndicatorPacket(this.getRandomX(0.5D), this.getY(MathHelper.nextDouble(this.random, 0.5D, 1.2D)), this.getRandomZ(0.5D), text, source, crit);
            ((ServerWorld) this.level).players().forEach(serverPlayerEntity -> NetworkManager.sendToClient(damageIndicatorPacket, serverPlayerEntity));
        }
    }

    @Override
    public boolean isCritical() {
        return this.isCritical;
    }

    @Override
    public void setCritical(boolean critical) {
        this.isCritical = critical;
    }
}
