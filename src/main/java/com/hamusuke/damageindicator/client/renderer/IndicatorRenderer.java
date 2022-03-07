package com.hamusuke.damageindicator.client.renderer;

import com.hamusuke.damageindicator.client.config.Config;
import com.hamusuke.damageindicator.math.AdditionalMathHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IndicatorRenderer {
    protected static final int maxAge = 20;
    private static final Minecraft mc = Minecraft.getInstance();
    protected static final float NORMAL = 1.0F;
    protected static final float CRITICAL = 2.5F;
    protected double prevPosX;
    protected double prevPosY;
    protected double prevPosZ;
    protected double x;
    protected double y;
    protected double z;
    protected float velocity;
    protected boolean dead;
    protected int age;
    protected final ITextComponent text;
    protected final String source;
    protected final boolean crit;
    protected int color;
    protected int textWidth = -1;
    protected long startedTickingTimeMs;
    protected final float distance;
    protected final float scaleMultiplier;
    protected float currentScale = Float.NaN;
    protected boolean paused;
    protected long passedTimeMs;

    public IndicatorRenderer(double x, double y, double z, ITextComponent text, String source, boolean crit, float distance) {
        this.setPos(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.text = text;
        this.source = source;
        this.crit = crit;
        this.syncIndicatorColor();
        this.distance = distance;
        this.scaleMultiplier = this.crit ? CRITICAL : NORMAL;
        this.startedTickingTimeMs = Util.getMillis();
    }

    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= maxAge) {
            this.markDead();
        } else if (this.age > maxAge / 2) {
            this.velocity += 0.008F;
            this.velocity *= 0.98F;
            this.moveOnHypotenuse3d(this.velocity);
        } else {
            if (this.currentScale != this.currentScale) {
                this.calculateScale(mc.isPaused());
            }

            this.moveOnHypotenuse3d(this.currentScale * (10.0F / (float) maxAge));
        }
    }

    private void moveOnHypotenuse3d(float lengthOfHypotenuseToMove) {
        if (mc.getEntityRenderDispatcher().camera == null) {
            this.markDead();
        } else {
            float phi = -mc.getEntityRenderDispatcher().camera.getYRot() * 0.017453292F;
            float theta = mc.getEntityRenderDispatcher().camera.getXRot() * 0.017453292F;
            float hypotenuse2d = lengthOfHypotenuseToMove * MathHelper.sin(theta);
            this.setPos(this.x + hypotenuse2d * MathHelper.sin(phi), this.y + lengthOfHypotenuseToMove * MathHelper.cos(theta), this.z + hypotenuse2d * MathHelper.cos(phi));
        }
    }

    public void render(MatrixStack matrix, IRenderTypeBuffer vertexConsumers, ActiveRenderInfo camera, float tickDelta) {
        FontRenderer textRenderer = mc.font;

        if (this.textWidth < 0) {
            this.textWidth = textRenderer.width(this.text);
        }

        if (this.textWidth == 0) {
            this.markDead();
        } else {
            float scale = this.calculateScale(mc.isPaused());
            double x = MathHelper.lerp(tickDelta, this.prevPosX, this.x);
            double y = MathHelper.lerp(tickDelta, this.prevPosY, this.y);
            double z = MathHelper.lerp(tickDelta, this.prevPosZ, this.z);
            Vector3d camPos = camera.getPosition();
            double camX = camPos.x;
            double camY = camPos.y;
            double camZ = camPos.z;

            matrix.pushPose();
            matrix.translate(x - camX, y - camY, z - camZ);
            matrix.mulPose(Vector3f.YP.rotationDegrees(-camera.getYRot()));
            matrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
            matrix.scale(-scale, -scale, scale);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);

            int alpha = 255;
            if (this.age > maxAge / 2) {
                alpha = (int) ((((float) (maxAge + 1) / (float) this.age) - 1.0F) * 255.0F);
            }
            alpha = MathHelper.clamp(alpha, 0, 255);

            int color = this.color;
            if (this.age <= 3) {
                color = AdditionalMathHelper.lerpColor((Util.getMillis() - this.startedTickingTimeMs) / (7.5F * (float) maxAge), color);
            }

            textRenderer.drawInBatch(this.text, -textRenderer.width(this.text) / 2.0F, -textRenderer.lineHeight / 2.0F, color + (alpha << 24), false, matrix.last().pose(), vertexConsumers, true, 0, 15728880);
            RenderSystem.disableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(true);
            matrix.popPose();
        }
    }

    private float calculateScale(boolean isPaused) {
        long timeDelta = Util.getMillis() - this.startedTickingTimeMs;
        float scale = AdditionalMathHelper.convexUpwardQuadraticFunction(MathHelper.clamp(timeDelta / (12.5F * (float) maxAge), 0.0F, 1.0F), this.crit ? -0.2F : -0.5F, this.crit ? 2.0F : 0.5F, 0.00375F * this.distance * 1.732050807F * this.scaleMultiplier, 0.0075F * this.distance * 1.732050807F * this.scaleMultiplier * this.scaleMultiplier * (this.crit ? 1.0F : 0.8F));
        scale -= 0.00025F * this.textWidth;
        scale = MathHelper.clamp(scale, 0.0001F, Float.MAX_VALUE);

        if (isPaused && !this.paused) {
            this.passedTimeMs = timeDelta;
            this.paused = true;
        } else if (isPaused) {
            return this.currentScale;
        } else if (this.paused) {
            this.startedTickingTimeMs = Util.getMillis() - this.passedTimeMs;
            this.paused = false;
            return this.calculateScale(false);
        }

        return this.currentScale = scale;
    }

    public void markDead() {
        this.dead = true;
    }

    public void syncIndicatorColor() {
        this.color = Config.CLIENT.colorConfig.getRGBFromDamageSource(Config.CLIENT.changeColorWhenCrit.get() && this.crit ? "critical" : this.source);
    }

    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isAlive() {
        return !this.dead;
    }
}
