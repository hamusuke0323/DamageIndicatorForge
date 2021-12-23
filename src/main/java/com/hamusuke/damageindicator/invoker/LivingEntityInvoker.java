package com.hamusuke.damageindicator.invoker;

import net.minecraft.client.resources.I18n;

public interface LivingEntityInvoker {
    default boolean isDeadOrDying() {
        return false;
    }

    default boolean isCritical() {
        return false;
    }

    default void send(String text, String source, boolean crit) {
    }

    default void sendImmune() {
        this.send(I18n.format("damageindicator.indicator.immune"), "immune", false);
    }

    default void setCritical(boolean critical) {
    }
}
