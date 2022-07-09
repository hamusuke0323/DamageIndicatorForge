function tick(renderer) {
    if (renderer.age++ >= maxAge) {
        renderer.markDead();
    } else if (renderer.age > maxAge / 2) {
        renderer.velocity += 0.008;
        renderer.velocity *= 0.98;
        renderer.moveOnHypotenuse3d(renderer.velocity);
    } else {
        renderer.moveOnHypotenuse3d(renderer.currentScale * (10.0 / maxAge));
    }
}

function calculateScale(timeDelta, renderer) {
    var scale = Packages.com.hamusuke.damageindicator.math.AdditionalMathHelper.convexUpwardQuadraticFunction(clamp(timeDelta / (12.5 * maxAge), 0.0, 1.0), renderer.crit ? -0.2 : -0.5, renderer.crit ? 2.0 : 0.5, 0.00375 * renderer.distance * 1.732050807 * renderer.scaleMultiplier, 0.0075 * renderer.distance * 1.732050807 * renderer.scaleMultiplier * renderer.scaleMultiplier * (renderer.crit ? 1.0 : 0.8));
    scale -= 0.00025 * renderer.textWidth;
    scale = clamp(scale, 0.0001, 20.0);
    return scale;
}

function clamp(number, min, max) {
    if (number < min) {
        return min;
    } else {
        return number > max ? max : number;
    }
}