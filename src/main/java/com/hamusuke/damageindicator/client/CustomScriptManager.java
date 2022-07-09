package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.function.Predicate;

@SideOnly(Side.CLIENT)
public class CustomScriptManager implements ISelectiveResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private final LinkedHashMap<ResourceLocation, String> scripts = Maps.newLinkedHashMap();
    @Nullable
    private Invocable invocable;

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        this.scripts.clear();
        DamageIndicatorClient.getInstance().clearQueue();

        String path = "script/custom_indicator_renderer.js";
        for (String domain : Lists.reverse(Lists.newArrayList(resourceManager.getResourceDomains()))) {
            ResourceLocation resourceLocation = new ResourceLocation(domain, path);
            try {
                IResource resource = resourceManager.getResource(resourceLocation);
                this.scripts.put(resourceLocation, StringUtils.join(IOUtils.readLines(resource.getInputStream(), StandardCharsets.UTF_8), '\n'));
            } catch (Exception ignored) {
            }
        }

        this.invocable = this.loadScript();
    }

    @Nullable
    private Invocable loadScript() {
        for (String script : this.scripts.values()) {
            try {
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine js = manager.getEngineByName("JavaScript");
                js.eval(script);
                return (Invocable) js;
            } catch (Exception e) {
                LOGGER.warn("Error occurred while loading JavaScript. Try to load the next script if it exists", e);
            }
        }

        return null;
    }

    @Nullable
    public Invocable getInvocable() {
        return this.invocable;
    }

    public void errorOccurred() {
        this.invocable = null;
    }
}
