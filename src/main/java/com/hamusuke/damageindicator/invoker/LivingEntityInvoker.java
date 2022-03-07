package com.hamusuke.damageindicator.invoker;

import java.util.Random;

public interface LivingEntityInvoker {
    default boolean canSendImmune(float amount) {
        return false;
    }

    default void send(String text, String source, boolean crit) {
    }

    default void sendImmune() {
    }

    Random getRandom();

    double getRandomX(double scale);

    double getRandomY(double scale);

    double getRandomZ(double scale);
}
