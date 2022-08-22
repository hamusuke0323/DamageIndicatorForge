package com.hamusuke.damageindicator.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin extends LivingEntityMixin {
    WitherBossMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void hurt(DamageSource p_31461_, float p_31462_, CallbackInfoReturnable<Boolean> cir) {
        if (!this.level.isClientSide && this.canSendImmune(p_31462_) && !cir.getReturnValueZ()) {
            this.sendImmune();
        }
    }
}
