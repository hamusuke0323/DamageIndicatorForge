package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.client.config.Config;
import com.hamusuke.damageindicator.client.renderer.IndicatorRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;
import java.util.Queue;

@OnlyIn(Dist.CLIENT)
public class DamageIndicatorClient {
    private static final Minecraft mc = Minecraft.getInstance();
    private final Queue<IndicatorRenderer> queue = Queues.newLinkedBlockingDeque();
    private static DamageIndicatorClient instance;
    private static final KeyBinding hideIndicator = new KeyBinding("key." + DamageIndicator.MOD_ID + ".hideIndicator.desc", -1, "key." + DamageIndicator.MOD_ID + ".category.indicator");

    public DamageIndicatorClient() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReload);
    }

    private void setup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(hideIndicator);
    }

    private void onConfigReload(final ModConfig.Reloading event) {
        if (event.getConfig().getModId().equals(DamageIndicator.MOD_ID)) {
            this.queue.forEach(IndicatorRenderer::syncIndicatorColor);
        }
    }

    @SubscribeEvent
    public void onKeyInput(final InputEvent.KeyInputEvent event) {
        if (hideIndicator.consumeClick()) {
            Config.CLIENT.hideIndicator.set(!Config.CLIENT.hideIndicator.get());
        }
    }

    @SubscribeEvent
    public void onEndTick(TickEvent.ClientTickEvent event) {
        if (!mc.isPaused() && event.phase == TickEvent.Phase.END) {
            List<IndicatorRenderer> list = Lists.newArrayList();
            this.queue.forEach(indicatorRenderer -> {
                indicatorRenderer.tick();
                if (!indicatorRenderer.isAlive()) {
                    list.add(indicatorRenderer);
                }
            });

            this.queue.removeAll(list);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(final RenderWorldLastEvent event) {
        if (!Config.CLIENT.hideIndicator.get() && !this.queue.isEmpty()) {
            mc.getProfiler().push("damage indicator rendering");
            IRenderTypeBuffer.Impl impl = mc.renderBuffers().bufferSource();
            MatrixStack matrices = event.getMatrixStack();
            matrices.clear();
            matrices.pushPose();
            this.queue.forEach(indicatorRenderer -> indicatorRenderer.render(matrices, impl, mc.getEntityRenderDispatcher().camera, event.getPartialTicks()));
            matrices.popPose();
            impl.endBatch();
            mc.getProfiler().pop();
        }
    }

    public void addRenderer(int entityId, ITextComponent text, String source, boolean crit) {
        if (mc.level != null && mc.player != null) {
            Entity clientEntity = mc.level.getEntity(entityId);

            if (clientEntity instanceof LivingEntity) {
                double x = clientEntity.getRandomX(0.5D);
                double y = clientEntity.getY(MathHelper.nextDouble(((LivingEntity) clientEntity).getRandom(), 0.5D, 1.2D));
                double z = clientEntity.getRandomZ(0.5D);
                Vector3d vector3d = new Vector3d(x, y, z);
                float distance = (float) mc.gameRenderer.getMainCamera().getPosition().distanceTo(vector3d);
                RayTraceResult result = mc.level.clip(new RayTraceContext(mc.player.position(), vector3d, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));
                if ((Config.CLIENT.forceIndicatorRendering.get() || result.getType() == RayTraceResult.Type.MISS) && distance <= (float) Config.CLIENT.renderDistance.get()) {
                    this.queue.add(new IndicatorRenderer(x, y, z, text, source, crit, distance));
                }
            }
        }
    }

    public static DamageIndicatorClient getInstance() {
        return instance;
    }
}
