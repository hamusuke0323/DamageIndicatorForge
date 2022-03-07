package com.hamusuke.damageindicator.invoker;

import net.minecraft.util.text.ITextComponent;

public interface LivingEntityInvoker {
    default boolean canSendImmune(float amount) {
        return false;
    }

    default void send(ITextComponent text, String source, boolean crit) {
    }

    default void sendImmune() {
    }
}
