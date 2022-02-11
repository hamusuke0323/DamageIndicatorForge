package com.hamusuke.damageindicator.invoker;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public interface LivingEntityInvoker {
    default boolean canSendImmune(float amount) {
        return false;
    }

    default void send(ITextComponent text, String source, boolean crit) {
    }

    default void sendImmune() {
        this.send(new TranslationTextComponent("damageindicator.indicator.immune"), "immune", false);
    }
}
