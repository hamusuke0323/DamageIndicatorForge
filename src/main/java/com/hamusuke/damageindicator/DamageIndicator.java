package com.hamusuke.damageindicator;

import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import com.hamusuke.damageindicator.client.config.Config;
import com.hamusuke.damageindicator.client.gui.screen.ConfigScreen;
import com.hamusuke.damageindicator.invoker.LivingEntityInvoker;
import com.hamusuke.damageindicator.network.Network;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
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
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(ConfigScreen::new));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new DamageIndicatorClient()));
        FMLJavaModLoadingContext.get().getModEventBus().addListener((final FMLCommonSetupEvent event) -> Network.load());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHeal(final LivingHealEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!event.isCanceled() && livingEntity instanceof LivingEntityInvoker invoker) {
            float amount = Math.min(livingEntity.getMaxHealth() - livingEntity.getHealth(), event.getAmount());
            if (!livingEntity.level.isClientSide && amount > 0.0F) {
                invoker.sendDMGPacketToAll(Component.literal("+" + Math.round(amount)), "heal", false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamageLast(final LivingDamageEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!livingEntity.level.isClientSide && livingEntity instanceof LivingEntityInvoker invoker) {
            DamageSource source = event.getSource();
            boolean bl = false;

            if (source.getEntity() instanceof com.hamusuke.criticalib.invoker.LivingEntityInvoker invoker1) {
                bl = invoker1.isCritical();
            }
            invoker.sendDMGPacketToAll(Component.literal("" + Math.round(event.getAmount())), source.getMsgId(), bl);
        }
    }
}
