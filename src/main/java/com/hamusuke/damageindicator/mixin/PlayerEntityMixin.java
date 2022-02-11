package com.hamusuke.damageindicator.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void damage(DamageSource p_36154_, float p_36155_, CallbackInfoReturnable<Boolean> cir) {
        if (!this.level.isClientSide && !cir.getReturnValueZ() && this.canSendImmune(p_36155_) && !((Object) this instanceof ServerPlayerEntity)) {
            this.sendImmune();
        }
    }
}
