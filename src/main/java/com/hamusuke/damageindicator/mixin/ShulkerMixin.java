package com.hamusuke.damageindicator.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Shulker.class)
public abstract class ShulkerMixin extends LivingEntityMixin {
    ShulkerMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void hurt(DamageSource p_33421_, float p_33422_, CallbackInfoReturnable<Boolean> cir) {
        if (!this.level.isClientSide && this.canSendImmune(p_33422_) && !cir.getReturnValueZ()) {
            this.sendImmune();
        }
    }
}
