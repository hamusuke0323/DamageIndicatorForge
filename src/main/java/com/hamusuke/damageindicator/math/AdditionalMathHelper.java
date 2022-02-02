package com.hamusuke.damageindicator.math;

import net.minecraft.util.math.MathHelper;

public class AdditionalMathHelper {
    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static int lerp(float delta, int start, int end) {
        return (int) (start + delta * (end - start));
    }

    public static float convexUpwardFunction2d(float delta, float minX, float maxX, float minY, float maxY) {
        return (float) (((minY - maxY) / Math.pow(MathHelper.absMax(minX, maxX), 2)) * Math.pow(lerp(delta, minX, maxX), 2) + maxY);
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }
}
