package com.hamusuke.damageindicator.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WitherEntity.class)
public abstract class WitherEntityMixin extends LivingEntityMixin {
    WitherEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void damage(DamageSource p_31461_, float p_31462_, CallbackInfoReturnable<Boolean> cir) {
        if (!this.level.isClientSide && this.canSendImmune(p_31462_) && !cir.getReturnValueZ()) {
            this.sendImmune();
        }
    }
}
