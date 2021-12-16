package com.hamusuke.damageindicator.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class IndicatorRenderer {
    private static final AxisAlignedBB EMPTY_BOUNDING_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
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
    protected final String text;
    protected final int color;
    protected int textWidth = -1;
    protected long startedTickingTimeMs;
    protected final float distance;
    protected final float scaleMultiplier;
    protected float currentScale = Float.NaN;
    protected boolean paused;
    protected long passedTimeMs;

    public IndicatorRenderer(double x, double y, double z, String text, int color, float distance, float scaleMultiplier) {
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
        this.color = color;
        this.distance = distance;
        this.scaleMultiplier = MathHelper.clamp(scaleMultiplier, 1.0F, 2.0F);
        this.startedTickingTimeMs = System.currentTimeMillis();
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

    public void render(float tickDelta) {
        Minecraft client = FMLClientHandler.instance().getClient();
        if (client.getRenderManager().renderViewEntity == null) {
            this.markDead();
            return;
        }

        if (this.textWidth < 0) {
            this.textWidth = client.fontRenderer.getStringWidth(this.text);
        }

        if (this.textWidth == 0) {
            this.markDead();
            return;
        }

        float scale = this.calculateScale(client.isGamePaused());
        double x = lerp(tickDelta, this.prevPosX, this.x);
        double y = lerp(tickDelta, this.prevPosY, this.y);
        double z = lerp(tickDelta, this.prevPosZ, this.z);
        double camX = client.getRenderManager().viewerPosX;
        double camY = client.getRenderManager().viewerPosY;
        double camZ = client.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x - camX, y - camY, z - camZ);
        GlStateManager.rotate(-client.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(client.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GL11.glNormal3d(0.0D, 0.0D, -1.0D * scale);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int l = 255;
        if (this.age > this.maxAge / 2 && this.age != 0) {
            l = (int) ((((float) (this.maxAge + 1) / (float) this.age) - 1.0F) * 255.0F);
        }
        l = MathHelper.clamp(l, 0, 255);
        client.fontRenderer.drawString(this.text, -this.textWidth / 2, -client.fontRenderer.FONT_HEIGHT / 2, this.color + (l << 24));
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    private float calculateScale(boolean isPaused) {
        long timeDelta = System.currentTimeMillis() - this.startedTickingTimeMs;
        float scale = lerp(timeDelta / 300.0F * this.scaleMultiplier, 0.025F * this.distance * 0.5F * this.scaleMultiplier * this.scaleMultiplier, 0.0125F * this.distance * 0.5F * this.scaleMultiplier);
        scale -= 0.00025 * this.textWidth;
        scale = MathHelper.clamp(scale, 0.0125F * this.distance * 0.5F * this.scaleMultiplier, 0.025F * this.distance * 0.5F * this.scaleMultiplier * this.scaleMultiplier);

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

    private static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    private static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    public void markDead() {
        this.dead = true;
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
                this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
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
