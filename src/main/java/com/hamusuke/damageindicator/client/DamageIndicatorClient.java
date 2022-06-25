package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.client.config.Config;
import com.hamusuke.damageindicator.client.renderer.IndicatorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;
import java.util.Queue;

@OnlyIn(Dist.CLIENT)
public class DamageIndicatorClient {
    private static final Minecraft mc = Minecraft.getInstance();
    private final Queue<IndicatorRenderer> queue = Queues.newLinkedBlockingDeque();
    private static DamageIndicatorClient instance;
    private static final KeyMapping hideIndicator = new KeyMapping("key." + DamageIndicator.MOD_ID + ".hideIndicator.desc", -1, "key." + DamageIndicator.MOD_ID + ".category.indicator");

    public DamageIndicatorClient() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReload);
    }

    private void setup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(hideIndicator);
    }

    private void onConfigReload(final ModConfigEvent event) {
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
    public void onRenderWorldLast(final RenderLevelLastEvent event) {
        if (!Config.CLIENT.hideIndicator.get() && !this.queue.isEmpty()) {
            mc.getProfiler().push("damage indicator rendering");
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            PoseStack matrices = event.getPoseStack();
            matrices.clear();
            matrices.pushPose();
            this.queue.forEach(indicatorRenderer -> indicatorRenderer.render(matrices, bufferSource, mc.getEntityRenderDispatcher().camera, event.getPartialTick()));
            matrices.popPose();
            bufferSource.endBatch();
            mc.getProfiler().pop();
        }
    }

    public void addRenderer(int entityId, Component text, String source, boolean crit) {
        if (mc.level != null && mc.player != null) {
            Entity clientEntity = mc.level.getEntity(entityId);

            if (clientEntity instanceof LivingEntity livingEntity) {
                double x = clientEntity.getRandomX(0.5D);
                double y = clientEntity.getY(Mth.nextDouble(livingEntity.getRandom(), 0.5D, 1.2D));
                double z = clientEntity.getRandomZ(0.5D);
                Vec3 vec3 = new Vec3(x, y, z);
                float distance = (float) mc.gameRenderer.getMainCamera().getPosition().distanceTo(vec3);
                BlockHitResult result = mc.level.clip(new ClipContext(mc.player.position(), vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                if ((Config.CLIENT.forceIndicatorRendering.get() || result.getType() == HitResult.Type.MISS) /*&& distance <= (float) Config.CLIENT.renderDistance.get()*/) {
                    this.queue.add(new IndicatorRenderer(x, y, z, text, source, crit, distance));
                }
            }
        }
    }

    public static DamageIndicatorClient getInstance() {
        return instance;
    }
}
