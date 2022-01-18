package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.hamusuke.damageindicator.client.renderer.IndicatorRenderer;
import com.hamusuke.damageindicator.config.ClientConfig;
import com.hamusuke.damageindicator.invoker.LivingEntityInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Queue;

@SideOnly(Side.CLIENT)
public class DamageIndicatorClient {
    public static final float NORMAL = 1.0F;
    public static final float CRITICAL = 2.0F;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final Queue<IndicatorRenderer> queue = Queues.newLinkedBlockingDeque();
    private static DamageIndicatorClient instance;

    public DamageIndicatorClient() {
        instance = this;
    }

    @SubscribeEvent
    public void onEndTick(TickEvent.ClientTickEvent event) {
        if (!mc.isGamePaused() && event.phase == TickEvent.Phase.END) {
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
        if (!mc.gameSettings.hideGUI && !this.queue.isEmpty()) {
            mc.mcProfiler.startSection("damage indicator rendering");
            this.queue.forEach(indicatorRenderer -> indicatorRenderer.render(event.getPartialTicks()));
            mc.mcProfiler.endSection();
        }
    }

    public void addRenderer(int entityId, String text, String source, boolean crit) {
        Entity clientEntity = mc.world.getEntityByID(entityId);
        Entity renderViewEntity = mc.getRenderViewEntity();

        if (clientEntity instanceof LivingEntityInvoker && renderViewEntity != null) {
            LivingEntityInvoker invoker = (LivingEntityInvoker) clientEntity;
            double x = invoker.getRandomX(0.5D);
            double y = invoker.getRandomY(MathHelper.nextDouble(invoker.getRandom(), 0.5D, 1.2D));
            double z = invoker.getRandomZ(0.5D);
            float distance = (float) Math.sqrt(mc.getRenderManager().getDistanceToCamera(x, y, z));
            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(x, y, z), new Vec3d(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY + renderViewEntity.getEyeHeight(), mc.getRenderManager().viewerPosZ));
            if ((ClientConfig.forceIndicatorRendering || result == null || result.typeOfHit != RayTraceResult.Type.BLOCK) && distance <= ClientConfig.renderDistance) {
                this.queue.add(new IndicatorRenderer(x, y, z, text, source, distance, crit));
            }
        }
    }

    public void syncIndicatorColor() {
        this.queue.forEach(IndicatorRenderer::syncIndicatorColor);
    }

    public static DamageIndicatorClient getInstance() {
        return instance;
    }
}
