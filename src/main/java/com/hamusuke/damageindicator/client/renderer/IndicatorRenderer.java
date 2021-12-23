package com.hamusuke.damageindicator.client.renderer;

import com.hamusuke.damageindicator.client.config.Config;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IndicatorRenderer {
    private static final AxisAlignedBB EMPTY_BOUNDING_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    protected static final float NORMAL = 1.0F;
    protected static final float CRITICAL = 2.0F;
    protected double prevPosX;
    protected double prevPosY;
    protected double prevPosZ;
    protected double x;
    protected double y;
    protected double z;
    protected double velocityX;
    protected double velocityY;
    protected double velocityZ;
    private AxisAlignedBB boundingBox;
    private boolean moveTooQuickly;
    protected boolean dead;
    protected float spacingXZ;
    protected float spacingY;
    protected int age;
    protected int maxAge;
    protected float gravityStrength;
    protected float g;
    protected final ITextComponent text;
    protected final String source;
    protected final boolean crit;
    protected int color;
    protected float textWidth = -1.0F;
    protected long startedTickingTimeMs;
    protected final float distance;
    protected final float scaleMultiplier;
    protected float currentScale = Float.NaN;
    protected boolean paused;
    protected long passedTimeMs;

    public IndicatorRenderer(double x, double y, double z, ITextComponent text, String source, boolean crit, float distance) {
        this.boundingBox = EMPTY_BOUNDING_BOX;
        this.spacingXZ = 0.6F;
        this.spacingY = 1.8F;
        this.g = 0.98F;
        this.setBoundingBoxSpacing(0.2F, 0.2F);
        this.setPos(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.maxAge = 20;
        this.gravityStrength = -0.2F;
        this.text = text;
        this.source = source;
        this.crit = crit;
        this.syncColorWithConfig();
        this.distance = distance;
        this.scaleMultiplier = this.crit ? CRITICAL : NORMAL;
        this.startedTickingTimeMs = Util.getMillis();
    }

    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else if (this.age > this.maxAge / 2) {
            this.velocityY -= 0.04D * (double) this.gravityStrength;
            this.move(this.velocityX, this.velocityY, this.velocityZ);
            this.velocityY *= this.g;
        }
    }

    public void render(MatrixStack matrix, IRenderTypeBuffer vertexConsumers, ActiveRenderInfo camera, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        FontRenderer textRenderer = client.font;

        if (this.textWidth < 0.0F) {
            this.textWidth = (float) textRenderer.width(this.text);
        }

        if (this.textWidth == 0.0F) {
            this.markDead();
            return;
        }

        float scale = this.calculateScale(client.isPaused());
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

        int l = 255;
        if (this.age > this.maxAge / 2 && this.age != 0) {
            l = (int) ((((float) (this.maxAge + 1) / (float) this.age) - 1.0F) * 255.0F);
        }
        l = MathHelper.clamp(l, 0, 255);
        textRenderer.drawInBatch(this.text, -textRenderer.width(this.text) / 2.0F, -textRenderer.lineHeight / 2.0F, this.color + (l << 24), false, matrix.last().pose(), vertexConsumers, true, 0, 15728880);
        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
        matrix.popPose();
    }

    private float calculateScale(boolean isPaused) {
        long timeDelta = Util.getMillis() - this.startedTickingTimeMs;
        float scale = MathHelper.lerp(timeDelta / 300.0F * this.scaleMultiplier, 0.025F * this.distance * 0.5F * this.scaleMultiplier * this.scaleMultiplier, 0.0125F * this.distance * 0.5F * this.scaleMultiplier);
        scale -= 0.00025 * this.textWidth;
        scale = MathHelper.clamp(scale, 0.0125F * this.distance * 0.5F * this.scaleMultiplier, 0.025F * this.distance * 0.5F * this.scaleMultiplier * this.scaleMultiplier);

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

    public void syncColorWithConfig() {
        this.color = Config.CLIENT.colorConfig.getRGBFromDamageSource(Config.CLIENT.changeColorWhenCrit.get() && this.crit ? "critical" : this.source);
    }

    protected void setBoundingBoxSpacing(float spacingXZ, float spacingY) {
        if (spacingXZ != this.spacingXZ || spacingY != this.spacingY) {
            this.spacingXZ = spacingXZ;
            this.spacingY = spacingY;
            AxisAlignedBB box = this.getBoundingBox();
            double d = (box.minX + box.maxX - (double) spacingXZ) / 2.0D;
            double e = (box.minZ + box.maxZ - (double) spacingXZ) / 2.0D;
            this.setBoundingBox(new AxisAlignedBB(d, box.minY, e, d + (double) this.spacingXZ, box.minY + (double) this.spacingY, e + (double) this.spacingXZ));
        }
    }

    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        float f = this.spacingXZ / 2.0F;
        float g = this.spacingY;
        this.setBoundingBox(new AxisAlignedBB(x - (double) f, y, z - (double) f, x + (double) f, y + (double) g, z + (double) f));
    }

    public void move(double dx, double dy, double dz) {
        if (!this.moveTooQuickly) {
            if (dx != 0.0D || dy != 0.0D || dz != 0.0D) {
                this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
                this.repositionFromBoundingBox();
            }

            if (Math.abs(dy) >= 9.999999747378752E-6D && Math.abs(dy) < 9.999999747378752E-6D) {
                this.moveTooQuickly = true;
            }

            if (dx != dx) {
                this.velocityX = 0.0D;
            }

            if (dz != dz) {
                this.velocityZ = 0.0D;
            }
        }
    }

    protected void repositionFromBoundingBox() {
        AxisAlignedBB box = this.getBoundingBox();
        this.x = (box.minX + box.maxX) / 2.0D;
        this.y = box.minY;
        this.z = (box.minZ + box.maxZ) / 2.0D;
    }

    public boolean isAlive() {
        return !this.dead;
    }

    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }
}
