package com.hamusuke.damageindicator.core;

import com.hamusuke.damageindicator.DamageIndicator;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Damage Indicator")
public class CoreMod implements IFMLLoadingPlugin {
    private static final Logger LOGGER = LogManager.getLogger("Damage Indicator Mod Core");

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        LOGGER.info("Mixin will boot!");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins." + DamageIndicator.MOD_ID + ".json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
