package com.hamusuke.damageindicator.invoker;

import net.minecraft.client.resources.I18n;

import static com.hamusuke.damageindicator.DamageIndicator.NORMAL;

public interface LivingEntityInvoker {
    default boolean isCritical() {
        return false;
    }

    default void send(String text, String source, float scaleMul) {
    }

    default void sendImmune() {
        this.send(I18n.format("damageindicator.indicator.immune"), "immune", NORMAL);
    }

    default void setCritical(boolean critical) {
    }
}
