package com.hamusuke.damageindicator.mixin;

import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityShulker.class)
public abstract class ShulkerEntityMixin extends LivingEntityMixin {
    ShulkerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.world.isRemote && this.canSendImmune(amount) && !cir.getReturnValue()) {
            this.sendImmune();
        }
    }
}
