package com.hamusuke.damageindicator.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends LivingEntityMixin {
    ServerPlayerMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void damage(DamageSource p_9037_, float p_9038_, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && this.canSendImmune(p_9038_)) {
            this.sendImmune();
        }
    }
}
