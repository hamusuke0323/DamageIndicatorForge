package com.hamusuke.damageindicator;

import com.hamusuke.criticalib.invoker.EntityLivingBaseInvoker;
import com.hamusuke.damageindicator.invoker.LivingEntityInvoker;
import com.hamusuke.damageindicator.network.NetworkManager;
import com.hamusuke.damageindicator.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = DamageIndicator.MOD_ID, name = DamageIndicator.MOD_NAME, version = DamageIndicator.VERSION, guiFactory = "com.hamusuke." + DamageIndicator.MOD_ID + ".client.gui.screen.ConfigScreenFactory", updateJSON = "https://raw.githubusercontent.com/hamusuke0323/DamageIndicatorForge/update/update.json", dependencies = "required-after:criticalib@[1.0.2,);")
public class DamageIndicator {
    public static final String MOD_ID = "damageindicator";
    public static final String MOD_NAME = "Damage Indicator";
    public static final String VERSION = "2.0.4";
    public static final Logger LOGGER = LogManager.getLogger();
    @SidedProxy(clientSide = "com.hamusuke.damageindicator.proxy.ClientProxy", serverSide = "com.hamusuke.damageindicator.proxy.CommonProxy")
    public static CommonProxy PROXY;
    private static Configuration config;

    public static Configuration getConfig() {
        return config;
    }

    private static void syncConfig(boolean load) {
        if (load) {
            config.load();
        }

        PROXY.onConfigChanged(config);
    }

    @Mod.EventHandler
    private void onSetup(final FMLPreInitializationEvent event) {
        NetworkManager.init();
        PROXY.preInit(event);
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        syncConfig(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHeal(final LivingHealEvent event) {
        EntityLivingBase livingEntity = event.getEntityLiving();
        if (!event.isCanceled() && livingEntity instanceof LivingEntityInvoker) {
            float amount = Math.min(livingEntity.getMaxHealth() - livingEntity.getHealth(), event.getAmount());
            if (!livingEntity.world.isRemote && amount > 0.0F) {
                ((LivingEntityInvoker) livingEntity).send("+" + MathHelper.ceil(amount), "heal", false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamageLast(final LivingDamageEvent event) {
        EntityLivingBase livingEntity = event.getEntityLiving();
        if (!livingEntity.world.isRemote && livingEntity instanceof LivingEntityInvoker) {
            DamageSource source = event.getSource();
            boolean bl = false;

            Entity entity = source.getTrueSource();
            if (entity instanceof LivingEntityInvoker) {
                bl = ((EntityLivingBaseInvoker) entity).isCritical();
            }
            ((LivingEntityInvoker) livingEntity).send("" + MathHelper.ceil(event.getAmount()), source.getDamageType(), bl);
        }
    }

    @SubscribeEvent
    public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (MOD_ID.equals(event.getModID())) {
            syncConfig(false);
        }
    }
}
