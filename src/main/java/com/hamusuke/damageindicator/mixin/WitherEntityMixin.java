package com.hamusuke.damageindicator.mixin;

import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityWither.class)
public abstract class WitherEntityMixin extends LivingEntityMixin {
    WitherEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.world.isRemote && this.canSendImmune(amount) && !cir.getReturnValue()) {
            this.sendImmune();
        }
    }
}
