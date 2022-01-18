package com.hamusuke.damageindicator.client.gui.screen;

import com.google.common.collect.Lists;
import com.hamusuke.damageindicator.DamageIndicator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ConfigScreen extends GuiConfig {
    public ConfigScreen(GuiScreen parentScreen) {
        super(parentScreen, getConfigElements(), DamageIndicator.MOD_ID, false, false, I18n.format("damageindicator.config.title"));
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> clients = Lists.newArrayList();
        clients.add(new DummyConfigElement.DummyCategoryElement("client", DamageIndicator.MOD_ID + ".category.clientsettings", Client.class));
        return clients;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        DamageIndicator.getConfig().save();
    }

    public static class Client extends GuiConfigEntries.CategoryEntry {
        public Client(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
            super(owningScreen, owningEntryList, configElement);
        }

        @Override
        protected GuiScreen buildChildScreen() {
            return new GuiConfig(this.owningScreen, new ConfigElement(DamageIndicator.getConfig().getCategory("client")).getChildElements(), this.owningScreen.modID, "client", this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart, this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart, GuiConfig.getAbridgedConfigPath(DamageIndicator.getConfig().toString()));
        }
    }
}
