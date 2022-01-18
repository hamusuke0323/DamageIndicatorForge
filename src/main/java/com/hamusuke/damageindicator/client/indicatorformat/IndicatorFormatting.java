package com.hamusuke.damageindicator.client.indicatorformat;

import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;

import java.util.function.BiFunction;

public enum IndicatorFormatting {
    INT_VALUE("int_value", (source, f) -> String.valueOf(MathHelper.ceil(f))),
    FLOAT_VALUE("float_value", (source, f) -> String.valueOf(f)),
    ENTITY_SOURCE("entity_source", (source, f) -> source.getTrueSource() == null ? "null" : source.getTrueSource().getName()),
    DAMAGE_SOURCE("damage_source", (source, f) -> source.getDamageType());

    private static final String prefix = "%";
    public final String formattingKey;
    private final BiFunction<DamageSource, Float, String> formatter;

    IndicatorFormatting(String formattingKey, BiFunction<DamageSource, Float, String> formatter) {
        this.formattingKey = formattingKey;
        this.formatter = formatter;
    }

    public String format(String input, DamageSource source, float amount) {
        for (IndicatorFormatting formatting : values()) {
            input = input.replace(prefix + formatting.formattingKey, formatting.formatter.apply(source, amount));
        }

        return input;
    }
}
