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
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(EntityLivingBase.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityInvoker {
    @Shadow
    public int maxHurtResistantTime;

    @Shadow
    public abstract float getHealth();

    protected int showImmuneCoolTime;
    @Shadow
    protected float lastDamage;

    LivingEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onEntityUpdate", at = @At("TAIL"))
    private void tick() {
        if (this.showImmuneCoolTime > 0) {
            this.showImmuneCoolTime--;
        }
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.world.isRemote && !cir.getReturnValueZ() && this.canSendImmune(amount)) {
            this.sendImmune();
        }
    }

    @Override
    public void send(String text, String source, boolean crit) {
        if (!this.world.isRemote) {
            DamageIndicatorPacket damageIndicatorPacket = new DamageIndicatorPacket(this.getEntityId(), text, source, crit);
            this.world.getMinecraftServer().getPlayerList().getPlayers().forEach(entityPlayer -> NetworkManager.sendToClient(damageIndicatorPacket, entityPlayer));
        }
    }

    @Override
    public Random getRandom() {
        return this.rand;
    }

    public double getRandomX(double scale) {
        return this.getPositionVector().x + this.width * (2.0D * this.rand.nextDouble() - 1.0D) * scale;
    }

    public double getRandomY(double scale) {
        return this.getPositionVector().y + this.height * scale;
    }

    public double getRandomZ(double scale) {
        return this.getPositionVector().z + this.width * (2.0D * this.rand.nextDouble() - 1.0D) * scale;
    }

    @Override
    public void sendImmune() {
        this.showImmuneCoolTime = 10;
        LivingEntityInvoker.super.sendImmune();
    }

    @Override
    public boolean canSendImmune(float amount) {
        return !((Object) this instanceof EntityPlayer) && !((Object) this instanceof EntityShulker) && !((Object) this instanceof EntityWither) && this.getHealth() > 0.0F && this.showImmuneCoolTime <= 0 && !((float) this.hurtResistantTime > (float) this.maxHurtResistantTime / 2.0F && amount <= this.lastDamage);
    }
}
