package com.hamusuke.damageindicator.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin {
    PlayerMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void hurt(DamageSource p_36154_, float p_36155_, CallbackInfoReturnable<Boolean> cir) {
        if (!this.level.isClientSide && !cir.getReturnValueZ() && this.canSendImmune(p_36155_) && !((Object) this instanceof ServerPlayer)) {
            this.sendImmune();
        }
    }
}
