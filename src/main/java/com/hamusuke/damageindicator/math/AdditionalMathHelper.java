package com.hamusuke.damageindicator.math;

import net.minecraft.util.math.MathHelper;

public class AdditionalMathHelper {
    public static int lerpColor(float delta, int rgb) {
        float multiplier = (float) MathHelper.clampedLerp(0.5F, 1.0F, delta);
        return MathHelper.color((((rgb >> 16) & 0xFF) / 255.0F) * multiplier, (((rgb >> 8) & 0xFF) / 255.0F) * multiplier, ((rgb & 0xFF) / 255.0F) * multiplier);
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static float convexUpwardQuadraticFunction(float delta, float minX, float maxX, float minY, float maxY) {
        return (float) (((minY - maxY) / Math.pow(MathHelper.absMax(minX, maxX), 2)) * Math.pow(lerp(delta, minX, maxX), 2) + maxY);
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }
}
