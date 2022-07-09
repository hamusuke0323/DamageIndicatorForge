package com.hamusuke.damageindicator.client.renderer;

import com.hamusuke.damageindicator.client.CustomScriptManager;
import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import com.hamusuke.damageindicator.config.ClientConfig;
import com.hamusuke.damageindicator.math.AdditionalMathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import javax.script.Invocable;

@SideOnly(Side.CLIENT)
public class IndicatorRenderer {
    public static final int maxAge = 20;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0F);
    private static final Minecraft mc = Minecraft.getMinecraft();
    protected double prevPosX;
    protected double prevPosY;
    protected double prevPosZ;
    protected double x;
    protected double y;
    protected double z;
    public final String text;
    protected boolean dead;
    public final String damageSourceType;
    public final boolean crit;
    public final int textWidth;
    public final float distance;
    protected int color;
    public final float scaleMultiplier;
    protected long startedTickingTimeMs;
    public float velocity;
    public int age;
    public float currentScale;
    protected boolean paused;
    protected long passedTimeMs;

    public IndicatorRenderer(double x, double y, double z, String text, String damageSourceType, float distance, boolean crit) {
        this.setPos(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.text = text;
        this.textWidth = mc.fontRenderer.getStringWidth(this.text);
        this.damageSourceType = damageSourceType;
        this.crit = crit;
        this.syncIndicatorColor();
        this.distance = distance;
        this.scaleMultiplier = this.crit ? DamageIndicatorClient.CRITICAL : DamageIndicatorClient.NORMAL;
        this.startedTickingTimeMs = System.currentTimeMillis();
    }

    private static CustomScriptManager getManager() {
        return DamageIndicatorClient.getInstance().getCustomScriptManager();
    }

    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        Invocable invocable = getManager().getInvocable();
        if (invocable != null) {
            try {
                invocable.invokeFunction("tick", this);
                return;
            } catch (Exception e) {
                getManager().errorOccurred();
                LOGGER.warn("Error occurred while invoking method", e);
            }
        }

        this.tickDefault();
    }

    private void tickDefault() {
        if (this.age++ >= maxAge) {
            this.markDead();
        } else if (this.age > maxAge / 2) {
            this.velocity += 0.008F;
            this.velocity *= 0.98F;
            this.moveOnHypotenuse3d(this.velocity);
        } else {
            this.moveOnHypotenuse3d(this.currentScale * (10.0F / (float) maxAge));
        }
    }

    public void moveOnHypotenuse3d(float lengthOfHypotenuseToMove) {
        float[] yawPitch = this.calculateAngle();
        float phi = yawPitch[0] * DEG_TO_RAD;
        float theta = yawPitch[1] * DEG_TO_RAD;
        float hypotenuse2d = lengthOfHypotenuseToMove * MathHelper.sin(theta);
        this.setPos(this.x + hypotenuse2d * MathHelper.sin(phi), this.y + lengthOfHypotenuseToMove * MathHelper.cos(theta), this.z + hypotenuse2d * MathHelper.cos(phi));
    }

    private float[] calculateAngle() {
        return mc.gameSettings.thirdPersonView == 2 ? new float[]{-mc.getRenderManager().playerViewY, -mc.getRenderManager().playerViewX} : new float[]{-mc.getRenderManager().playerViewY, mc.getRenderManager().playerViewX};
    }

    public void render(float tickDelta) {
        Entity renderViewEntity = mc.getRenderManager().renderViewEntity;

        if (renderViewEntity == null || this.textWidth <= 0) {
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
            int alpha = 255;
            if (this.age > maxAge / 2) {
                alpha = (int) (((float) (maxAge + 1) / (float) this.age - 1.0F) * 255.0F);
            }
            alpha = MathHelper.clamp(alpha, 0, 255);

            int color = this.color;
            if (this.age <= 3) {
                color = AdditionalMathHelper.lerpColor((System.currentTimeMillis() - this.startedTickingTimeMs) / (7.5F * (float) maxAge), color);
            }

            mc.fontRenderer.drawString(this.text, -this.textWidth / 2, -mc.fontRenderer.FONT_HEIGHT / 2, color + (alpha << 24));
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        }
    }

    private float calculateScale(boolean isPaused) {
        long timeDelta = System.currentTimeMillis() - this.startedTickingTimeMs;
        float scale;

        Invocable invocable = getManager().getInvocable();
        if (invocable != null) {
            try {
                scale = (float) (double) invocable.invokeFunction("calculateScale", timeDelta, this);
            } catch (Exception e) {
                getManager().errorOccurred();
                LOGGER.warn("Error occurred while invoking method", e);
                scale = this.calculateScaleDefault(timeDelta);
            }
        } else {
            scale = this.calculateScaleDefault(timeDelta);
        }

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

    private float calculateScaleDefault(long timeDelta) {
        float scale = AdditionalMathHelper.convexUpwardQuadraticFunction(MathHelper.clamp(timeDelta / (12.5F * (float) maxAge), 0.0F, 1.0F), this.crit ? -0.2F : -0.5F, this.crit ? 2.0F : 0.5F, 0.00375F * this.distance * 1.732050807F * this.scaleMultiplier, 0.0075F * this.distance * 1.732050807F * this.scaleMultiplier * this.scaleMultiplier * (this.crit ? 1.0F : 0.8F));
        scale -= 0.00025F * this.textWidth;
        scale = MathHelper.clamp(scale, 0.0001F, 20.0F);
        return scale;
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
