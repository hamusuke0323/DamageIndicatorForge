package com.hamusuke.damageindicator.invoker;

import net.minecraft.network.chat.Component;

public interface LivingEntityInvoker {
    default boolean canSendImmune(float amount) {
        return false;
    }

    default void send(Component text, String source, boolean crit) {
    }

    default void sendImmune() {
    }
}
