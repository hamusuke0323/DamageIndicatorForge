package com.hamusuke.damageindicator.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    PlayerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.world.isRemote && !cir.getReturnValueZ() && this.canSendImmune(amount)) {
            this.sendImmune();
        }
    }

    @Override
    public boolean canSendImmune(float amount) {
        return super.canSendImmune(amount) && !((Object) this instanceof EntityPlayerMP);
    }
}
