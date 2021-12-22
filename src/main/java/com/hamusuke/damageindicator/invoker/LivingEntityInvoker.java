package com.hamusuke.damageindicator.invoker;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import static com.hamusuke.damageindicator.DamageIndicator.NORMAL;

public interface LivingEntityInvoker {
    default boolean isCritical() {
        return false;
    }

    default void send(ITextComponent text, String source, float scaleMul) {
    }

    default void sendImmune() {
        this.send(new TranslationTextComponent("damageindicator.indicator.immune"), "immune", NORMAL);
    }

    default void setCritical(boolean critical) {
    }
}
