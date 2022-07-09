package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hamusuke.damageindicator.client.renderer.IndicatorRenderer;
import com.hamusuke.damageindicator.math.AdditionalMathHelper;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
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
import java.util.List;
import java.util.function.Predicate;

@SideOnly(Side.CLIENT)
public class CustomScriptManager implements ISelectiveResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private final ScriptEngine js;
    private final LinkedHashMap<ResourceLocation, String> scripts = Maps.newLinkedHashMap();
    @Nullable
    private Invocable invocable;

    public CustomScriptManager() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        this.js = scriptEngineManager.getEngineByName("rhino");
        LOGGER.info("Got Rhino JavaScript Engine or not: {}", this.js == null ? "null" : "got");

        if (this.js != null) {
            this.js.put("MathHelper", new MathHelper());
            this.js.put("AdditionalMathHelper", new AdditionalMathHelper());
            this.js.put("maxAge", IndicatorRenderer.maxAge);
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        this.scripts.clear();
        DamageIndicatorClient.getInstance().clearQueue();

        String path = "script/custom_indicator_renderer.js";
        for (String domain : Lists.reverse(Lists.newArrayList(resourceManager.getResourceDomains()))) {
            ResourceLocation resourceLocation = new ResourceLocation(domain, path);
            try {
                IResource resource = resourceManager.getResource(resourceLocation);
                this.registerScript(resourceLocation, IOUtils.readLines(resource.getInputStream(), StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
        }

        this.invocable = this.loadScript();
    }

    private void registerScript(ResourceLocation resourceLocation, List<String> script) {
        String str = StringUtils.join(script, '\n');
        this.scripts.put(resourceLocation, str);
        LOGGER.info("Registered animation script:\n{}", str);
    }

    @Nullable
    private Invocable loadScript() {
        if (this.js != null) {
            for (String script : this.scripts.values()) {
                try {
                    this.js.eval(script);
                    return (Invocable) this.js;
                } catch (Exception e) {
                    LOGGER.warn("Error occurred while loading JavaScript. Try to load the next script if it exists", e);
                }
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
