package com.hamusuke.damageindicator.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends LivingEntityMixin {
    ServerPlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void damage(DamageSource p_9037_, float p_9038_, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && this.canSendImmune(p_9038_)) {
            this.sendImmune();
        }
    }
}
