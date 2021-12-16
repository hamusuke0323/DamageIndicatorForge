package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.hamusuke.damageindicator.client.renderer.IndicatorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Queue;

@SideOnly(Side.CLIENT)
public class DamageIndicatorClient {
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

    public void addRenderer(double x, double y, double z, String text, int color, float scaleMul) {
        float distance = (float) Math.sqrt(mc.getRenderManager().getDistanceToCamera(x, y, z));
        this.queue.add(new IndicatorRenderer(x, y, z, text, color, distance, scaleMul));
    }

    public static DamageIndicatorClient getInstance() {
        return instance;
    }
}
