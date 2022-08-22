package com.hamusuke.damageindicator.math;

import net.minecraft.util.Mth;

public class AdditionalMathHelper {
    public static int lerpColor(float delta, int rgb) {
        float multiplier = Mth.clampedLerp(0.5F, 1.0F, delta);
        return Mth.color((((rgb >> 16) & 0xFF) / 255.0F) * multiplier, (((rgb >> 8) & 0xFF) / 255.0F) * multiplier, ((rgb & 0xFF) / 255.0F) * multiplier);
    }

    public static float convexUpwardQuadraticFunction(float delta, float minX, float maxX, float minY, float maxY) {
        return (float) (((minY - maxY) / Math.pow(Mth.absMax(minX, maxX), 2)) * Math.pow(Mth.lerp(delta, minX, maxX), 2) + maxY);
    }
}
