package com.hamusuke.damageindicator.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hamusuke.damageindicator.DamageIndicator;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.awt.*;
import java.util.List;
import java.util.Locale;

@Config(modid = DamageIndicator.MOD_ID, category = "color")
public class DamageIndicatorConfigColor {
    @Config.Ignore
    private static final List<RGB> configs = Lists.newArrayList();
    public static RGB inFireDamage = register("infiredamage", 255, 150, 0);
    public static RGB lightningBoltDamage = register("lightningboltdamage");
    public static RGB onFireDamage = register("onfiredamage", 255, 150, 0);
    public static RGB lavaDamage = register("lavadamage", 255, 150, 0);
    public static RGB hotFloorDamage = register("hotfloordamage", 255, 150, 0);
    public static RGB inWallDamage = register("inwalldamage", 255, 225, 0);
    public static RGB crammingDamage = register("crammingdamage");
    public static RGB drownDamage = register("drowndamage");
    public static RGB starveDamage = register("starvedamage");
    public static RGB cactusDamage = register("cactusdamage");
    public static RGB fallDamage = register("falldamage", 255, 225, 0);
    public static RGB flyIntoWallDamage = register("flyintowalldamage", 255, 225, 0);
    public static RGB outOfWorldDamage = register("outofworlddamage", 0, 0, 0);
    public static RGB genericDamage = register("genericdamage");
    public static RGB magicDamage = register("magicdamage");
    public static RGB witherDamage = register("witherdamage");
    public static RGB anvilDamage = register("anvildamage", 255, 225, 0);
    public static RGB fallingBlockDamage = register("fallingblockdamage", 255, 225, 0);
    public static RGB dragonBreathDamage = register("dragonbreathdamage");
    public static RGB fireworksDamage = register("fireworksdamage");
    public static RGB heal = register("heal", 85, 255, 85);
    public static RGB immune = register("immune", 170, 170, 170);

    private static RGB register(String name) {
        return register(name, 255, 255, 255);
    }

    private static RGB register(String name, int red, int green, int blue) {
        return register(new RGB(name, red, green, blue));
    }

    private static RGB register(RGB rgb) {
        configs.add(rgb);
        return rgb;
    }

    public static void sync(Configuration config) {
        configs.forEach(rgb -> rgb.sync(config));
    }

    public static int getColorFromDamageSourceType(String type) {
        if (type != null) {
            for (RGB rgb : configs) {
                if (rgb.name.contains(type.toLowerCase(Locale.ROOT))) {
                    return rgb.toRGBColor();
                }
            }
        }

        return 16777215;
    }

    public static class RGB {
        @Config.Ignore
        private final String name;
        public int red;
        public int green;
        public int blue;

        public RGB(String name, int red, int green, int blue) {
            this.name = name;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public int toRGBColor() {
            return new Color(this.red, this.green, this.blue).getRGB();
        }

        public void sync(Configuration config) {
            config.setCategoryPropertyOrder("color." + this.name, ImmutableList.of("red", "green", "blue"));
            config.setCategoryLanguageKey("color." + this.name, DamageIndicator.MOD_ID + ".category." + this.name);

            Property red = config.getCategory("color." + this.name).get("red");
            red.setMaxValue(255);
            red.setMinValue(0);
            red.setHasSlidingControl(true);
            this.red = red.getInt(255);

            Property green = config.getCategory("color." + this.name).get("green");
            green.setMaxValue(255);
            green.setMinValue(0);
            green.setHasSlidingControl(true);
            this.green = green.getInt(255);

            Property blue = config.getCategory("color." + this.name).get("blue");
            blue.setMaxValue(255);
            blue.setMinValue(0);
            blue.setHasSlidingControl(true);
            this.blue = blue.getInt(255);
        }
    }
}
