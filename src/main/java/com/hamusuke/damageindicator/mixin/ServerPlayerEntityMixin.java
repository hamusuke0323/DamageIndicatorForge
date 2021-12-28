package com.hamusuke.damageindicator.mixin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerMP.class)
public abstract class ServerPlayerEntityMixin extends LivingEntityMixin {
    ServerPlayerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && this.canSendImmune(amount)) {
            this.sendImmune();
        }
    }
}
