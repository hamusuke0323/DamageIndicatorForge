package com.hamusuke.damageindicator.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerEntity.class)
public abstract class ShulkerEntityMixin extends LivingEntityMixin {
    ShulkerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void damage(DamageSource p_33421_, float p_33422_, CallbackInfoReturnable<Boolean> cir) {
        if (!this.level.isClientSide && this.canSendImmune(p_33422_) && !cir.getReturnValueZ()) {
            this.sendImmune();
        }
    }
}
