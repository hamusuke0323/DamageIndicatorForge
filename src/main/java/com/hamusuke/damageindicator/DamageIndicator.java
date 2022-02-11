package com.hamusuke.damageindicator;

import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import com.hamusuke.damageindicator.client.config.Config;
import com.hamusuke.damageindicator.client.gui.screen.ConfigScreen;
import com.hamusuke.damageindicator.invoker.LivingEntityInvoker;
import com.hamusuke.damageindicator.network.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DamageIndicator.MOD_ID)
public class DamageIndicator {
    public static final String MOD_ID = "damageindicator";

    public DamageIndicator() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CONFIG);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> ConfigScreen::new);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new DamageIndicatorClient()));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onSetup(final FMLCommonSetupEvent event) {
        NetworkManager.init();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHeal(final LivingHealEvent event) {
        LivingEntity livingEntity = event.getEntityLiving();
        if (!event.isCanceled() && livingEntity instanceof LivingEntityInvoker) {
            float amount = Math.min(livingEntity.getMaxHealth() - livingEntity.getHealth(), event.getAmount());
            if (!livingEntity.level.isClientSide && amount > 0.0F) {
                ((LivingEntityInvoker) livingEntity).send(new StringTextComponent("+" + MathHelper.ceil(amount)), "heal", false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamageLast(final LivingDamageEvent event) {
        LivingEntity livingEntity = event.getEntityLiving();
        if (!livingEntity.level.isClientSide && livingEntity instanceof LivingEntityInvoker) {
            DamageSource source = event.getSource();
            boolean bl = false;

            Entity entity = source.getEntity();
            if (entity instanceof com.hamusuke.criticalib.invoker.LivingEntityInvoker) {
                bl = ((com.hamusuke.criticalib.invoker.LivingEntityInvoker) entity).isCritical();
            }
            ((LivingEntityInvoker) livingEntity).send(new StringTextComponent("" + MathHelper.ceil(event.getAmount())), source.getMsgId(), bl);
        }
    }
}
