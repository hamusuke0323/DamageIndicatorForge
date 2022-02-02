package com.hamusuke.damageindicator.client.renderer;

import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import com.hamusuke.damageindicator.config.ClientConfig;
import com.hamusuke.damageindicator.math.AdditionalMathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class IndicatorRenderer {
    protected static final int maxAge = 20;
    private static final Minecraft mc = Minecraft.getMinecraft();
    protected double prevPosX;
    protected double prevPosY;
    protected double prevPosZ;
    protected double x;
    protected double y;
    protected double z;
    protected float velocity;
    protected boolean dead;
    protected int age;
    protected final String text;
    protected final String damageSourceType;
    protected final boolean crit;
    protected int color;
    protected int textWidth = -1;
    protected long startedTickingTimeMs;
    protected final float distance;
    protected final float scaleMultiplier;
    protected float currentScale = Float.NaN;
    protected boolean paused;
    protected long passedTimeMs;

    public IndicatorRenderer(double x, double y, double z, String text, String damageSourceType, float distance, boolean crit) {
        this.setPos(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.text = text;
        this.damageSourceType = damageSourceType;
        this.crit = crit;
        this.syncIndicatorColor();
        this.distance = distance;
        this.scaleMultiplier = this.crit ? DamageIndicatorClient.CRITICAL : DamageIndicatorClient.NORMAL;
        this.startedTickingTimeMs = System.currentTimeMillis();
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
            this.moveOnRadius(this.velocity);
        } else {
            if (this.currentScale != this.currentScale) {
                this.calculateScale(mc.isGamePaused());
            }

            this.moveOnRadius(this.currentScale * 0.5F);
        }
    }

    private void moveOnRadius(float amountToMove) {
        float[] yawPitch = this.calculateAngle();
        float phi = yawPitch[0] * 0.017453292F;
        float theta = yawPitch[1] * 0.017453292F;
        float radius2d = amountToMove * MathHelper.sin(theta);
        this.setPos(this.x + radius2d * MathHelper.sin(phi), this.y + amountToMove * MathHelper.cos(theta), this.z + radius2d * MathHelper.cos(phi));
    }

    private float[] calculateAngle() {
        return mc.gameSettings.thirdPersonView == 2 ? new float[]{-mc.getRenderManager().playerViewY, -mc.getRenderManager().playerViewX} : new float[]{-mc.getRenderManager().playerViewY, mc.getRenderManager().playerViewX};
    }

    public void render(float tickDelta) {
        Entity renderViewEntity = mc.getRenderManager().renderViewEntity;

        if (this.textWidth < 0) {
            this.textWidth = mc.fontRenderer.getStringWidth(this.text);
        }

        if (renderViewEntity == null || this.textWidth == 0) {
            this.markDead();
        } else {
            float scale = this.calculateScale(mc.isGamePaused());
            double x = AdditionalMathHelper.lerp(tickDelta, this.prevPosX, this.x);
            double y = AdditionalMathHelper.lerp(tickDelta, this.prevPosY, this.y);
            double z = AdditionalMathHelper.lerp(tickDelta, this.prevPosZ, this.z);
            double camX = mc.getRenderManager().viewerPosX;
            double camY = mc.getRenderManager().viewerPosY;
            double camZ = mc.getRenderManager().viewerPosZ;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x - camX, y - camY, z - camZ);
            float[] yawPitch = this.calculateAngle();
            GlStateManager.rotate(yawPitch[0], 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(yawPitch[1], 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-scale, -scale, scale);
            GL11.glNormal3d(0.0D, 0.0D, -1.0D * scale);
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            int l = 255;
            if (this.age > maxAge / 2) {
                l = (int) ((((float) (maxAge + 1) / (float) this.age) - 1.0F) * 255.0F);
            }
            l = MathHelper.clamp(l, 0, 255);

            int color = this.color;
            if (this.age <= 3) {
                color /= 255.0D / AdditionalMathHelper.lerp(MathHelper.clamp((System.currentTimeMillis() - this.startedTickingTimeMs) / 150.0F, 0.0F, 1.0F), 1, 255);
            }

            mc.fontRenderer.drawString(this.text, -this.textWidth / 2, -mc.fontRenderer.FONT_HEIGHT / 2, color + (l << 24));
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        }
    }

    private float calculateScale(boolean isPaused) {
        long timeDelta = System.currentTimeMillis() - this.startedTickingTimeMs;
        float scale = AdditionalMathHelper.convexUpwardFunction2d(MathHelper.clamp(timeDelta / 250.0F, 0.0F, 1.0F), this.crit ? -0.2F : -0.5F, this.crit ? 2.0F : 0.5F, 0.00375F * this.distance * 1.732050807F * this.scaleMultiplier, 0.0075F * this.distance * 1.732050807F * this.scaleMultiplier * this.scaleMultiplier);
        scale -= 0.00025 * this.textWidth;
        scale = MathHelper.clamp(scale, 0.0001F, Float.MAX_VALUE);

        if (isPaused && !this.paused) {
            this.passedTimeMs = timeDelta;
            this.paused = true;
        } else if (isPaused) {
            return this.currentScale;
        } else if (this.paused) {
            this.startedTickingTimeMs = System.currentTimeMillis() - this.passedTimeMs;
            this.paused = false;
            return this.calculateScale(false);
        }

        return this.currentScale = scale;
    }

    public void syncIndicatorColor() {
        this.color = ClientConfig.ColorConfig.getColorFromDamageSourceType(ClientConfig.changeColorWhenCrit && this.crit ? "critical" : this.damageSourceType);
    }

    public void markDead() {
        this.dead = true;
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
