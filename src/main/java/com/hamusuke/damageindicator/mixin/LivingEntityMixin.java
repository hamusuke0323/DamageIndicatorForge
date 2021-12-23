package com.hamusuke.damageindicator.mixin;

import com.hamusuke.damageindicator.invoker.LivingEntityInvoker;
import com.hamusuke.damageindicator.network.DamageIndicatorPacket;
import com.hamusuke.damageindicator.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityInvoker {
    @Shadow
    public abstract float getHealth();

    private boolean isCritical;

    LivingEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof EntityPlayer) && !((Object) this instanceof EntityShulker) && !((Object) this instanceof EntityWither)) {
            if (!this.world.isRemote && !cir.getReturnValue() && !this.isDeadOrDying()) {
                this.sendImmune();
            }
        }
    }

    @Override
    public void send(String text, String source, boolean crit) {
        if (!this.world.isRemote) {
            DamageIndicatorPacket damageIndicatorPacket = new DamageIndicatorPacket(this.getRandomX(0.5D), this.getRandomY(MathHelper.nextDouble(this.rand, 0.5D, 1.2D)), this.getRandomZ(0.5D), text, source, crit);
            this.world.getMinecraftServer().getPlayerList().getPlayers().forEach(entityPlayer -> NetworkManager.sendToClient(damageIndicatorPacket, entityPlayer));
        }
    }

    private double getRandomX(double scale) {
        return this.getPositionVector().x + this.width * (2.0D * this.rand.nextDouble() - 1.0D) * scale;
    }

    private double getRandomY(double scale) {
        return this.getPositionVector().y + this.height * scale;
    }

    private double getRandomZ(double scale) {
        return this.getPositionVector().z + this.width * (2.0D * this.rand.nextDouble() - 1.0D) * scale;
    }

    @Override
    public boolean isDeadOrDying() {
        return this.getHealth() <= 0.0F;
    }

    @Override
    public boolean isCritical() {
        return this.isCritical;
    }

    @Override
    public void setCritical(boolean critical) {
        this.isCritical = critical;
    }
}
