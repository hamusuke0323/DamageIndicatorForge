package com.hamusuke.damageindicator;

import com.hamusuke.damageindicator.invoker.LivingEntityInvoker;
import com.hamusuke.damageindicator.network.NetworkManager;
import com.hamusuke.damageindicator.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = DamageIndicator.MOD_ID, name = DamageIndicator.MOD_NAME, version = DamageIndicator.VERSION)
public class DamageIndicator {
    public static final String MOD_ID = "damageindicator";
    public static final String MOD_NAME = "Damage Indicator";
    public static final String VERSION = "1.0.0";
    public static final float NORMAL = 1.0F;
    public static final float CRITICAL = 2.0F;
    @SidedProxy(clientSide = "com.hamusuke.damageindicator.proxy.ClientProxy", serverSide = "com.hamusuke.damageindicator.proxy.CommonProxy")
    public static CommonProxy PROXY;

    @Mod.EventHandler
    private void onSetup(final FMLPreInitializationEvent event) {
        NetworkManager.init();
        PROXY.preInit();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHeal(final LivingHealEvent event) {
        EntityLivingBase livingEntity = event.getEntityLiving();
        if (!event.isCanceled() && livingEntity instanceof LivingEntityInvoker) {
            float amount = Math.min(livingEntity.getMaxHealth() - livingEntity.getHealth(), event.getAmount());
            if (!livingEntity.world.isRemote && amount > 0.0F) {
                ((LivingEntityInvoker) livingEntity).send("+" + MathHelper.ceil(amount), 5635925, NORMAL);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCritHitLast(final CriticalHitEvent event) {
        LivingEntityInvoker invoker = (LivingEntityInvoker) event.getEntityPlayer();
        if (!event.getEntityPlayer().world.isRemote && !invoker.isCritical()) {
            invoker.setCritical(event.getResult() == Event.Result.ALLOW || (event.isVanillaCritical() && event.getResult() == Event.Result.DEFAULT));
        }
    }

    @SubscribeEvent
    public void onHurt(final LivingHurtEvent event) {
        EntityLivingBase livingEntity = event.getEntityLiving();
        if (!livingEntity.world.isRemote) {
            DamageSource source = event.getSource();
            if (source instanceof EntityDamageSourceIndirect && source.getTrueSource() instanceof LivingEntityInvoker && source.getImmediateSource() instanceof EntityArrow) {
                LivingEntityInvoker livingEntityInvoker = (LivingEntityInvoker) source.getTrueSource();
                livingEntityInvoker.setCritical(((EntityArrow) source.getImmediateSource()).getIsCritical());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamageLast(final LivingDamageEvent event) {
        EntityLivingBase livingEntity = event.getEntityLiving();
        if (!livingEntity.world.isRemote && livingEntity instanceof LivingEntityInvoker) {
            LivingEntityInvoker invoker = (LivingEntityInvoker) livingEntity;
            DamageSource source = event.getSource();
            float scaleMul = NORMAL;

            Entity entity = source.getTrueSource();
            if (entity instanceof LivingEntityInvoker) {
                LivingEntityInvoker livingEntityInvoker = (LivingEntityInvoker) entity;
                if (livingEntityInvoker.isCritical()) {
                    scaleMul = CRITICAL;
                    livingEntityInvoker.setCritical(false);
                }
            }
            invoker.send("" + MathHelper.ceil(event.getAmount()), getColorFromDamageSource(source), scaleMul);
        }
    }

    private static int getColorFromDamageSource(DamageSource source) {
        if (source.isFireDamage()) {
            return 16750080;
        } else if (source == DamageSource.FALL || source == DamageSource.FALLING_BLOCK || source == DamageSource.IN_WALL) {
            return 16769280;
        } else if (source.canHarmInCreative()) {
            return 0;
        }

        return 16777215;
    }
}
