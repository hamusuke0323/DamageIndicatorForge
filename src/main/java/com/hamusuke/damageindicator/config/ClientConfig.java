package com.hamusuke.damageindicator.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.hamusuke.damageindicator.DamageIndicator;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.awt.*;
import java.util.Objects;
import java.util.Set;

@Config(modid = DamageIndicator.MOD_ID, category = "client")
public class ClientConfig {
    public static String indicatorTextFormat = "%int_value";
    public static boolean forceIndicatorRendering;
    public static int renderDistance = 64;
    public static boolean changeColorWhenCrit;

    public static void sync(Configuration config) {
        //Property indicatorTextFormat = config.get("client", "indicatorTextFormat", "%int_value");
        //ClientConfig.indicatorTextFormat = indicatorTextFormat.getString();

        Property forceIndicatorRendering = config.get("client", "forceIndicatorRendering", false);
        ClientConfig.forceIndicatorRendering = forceIndicatorRendering.getBoolean(false);

        Property renderDistance = config.get("client", "renderDistance", 64);
        renderDistance.setHasSlidingControl(true);
        renderDistance.setMaxValue(256);
        renderDistance.setMinValue(0);
        ClientConfig.renderDistance = renderDistance.getInt(64);

        Property changeColorWhenCrit = config.get("client", "changeColorWhenCrit", false);
        changeColorWhenCrit.setLanguageKey(DamageIndicator.MOD_ID + ".config.color.crit");
        ClientConfig.changeColorWhenCrit = changeColorWhenCrit.getBoolean(false);

        ColorConfig.configs.forEach(rgb -> rgb.sync(config));

        if (config.hasCategory("color")) {
            config.removeCategory(config.getCategory("color"));
        }
    }

    @Config(modid = DamageIndicator.MOD_ID, category = "client.color")
    public static class ColorConfig {
        @Config.Ignore
        private static final Set<RGB> configs = Sets.newHashSet();
        public static RGB inFireDamage = register("infiredamage", 255, 150, 0);
        public static RGB lightningBoltDamage = register("lightningboltdamage", 255, 80, 255);
        public static RGB onFireDamage = register("onfiredamage", 255, 150, 0);
        public static RGB lavaDamage = register("lavadamage", 255, 150, 0);
        public static RGB hotFloorDamage = register("hotfloordamage", 255, 150, 0);
        public static RGB inWallDamage = register("inwalldamage", 255, 225, 0);
        public static RGB crammingDamage = register("crammingdamage", 255, 225, 0);
        public static RGB drownDamage = register("drowndamage", 0, 20, 255);
        public static RGB starveDamage = register("starvedamage", 150, 100, 0);
        public static RGB cactusDamage = register("cactusdamage", 0, 255, 0);
        public static RGB fallDamage = register("falldamage", 255, 225, 0);
        public static RGB flyIntoWallDamage = register("flyintowalldamage", 255, 225, 0);
        public static RGB outOfWorldDamage = register("outofworlddamage", 0, 0, 0);
        public static RGB genericDamage = register("genericdamage");
        public static RGB magicDamage = register("magicdamage", 0, 255, 160);
        public static RGB witherDamage = register("witherdamage", 25, 25, 25);
        public static RGB anvilDamage = register("anvildamage", 255, 225, 0);
        public static RGB fallingBlockDamage = register("fallingblockdamage", 255, 225, 0);
        public static RGB dragonBreathDamage = register("dragonbreathdamage");
        public static RGB fireworksDamage = register("fireworksdamage");
        public static RGB critical = register("critical", 255, 255, 0);
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

        public static int getColorFromDamageSourceType(String type) {
            if (type != null) {
                for (RGB rgb : configs) {
                    if (rgb.name.replace("damage", "").equalsIgnoreCase(type)) {
                        return rgb.toRGBColor();
                    }
                }
            }

            return 16777215;
        }

        public static class RGB {
            @Config.Ignore
            protected final String name;
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
                config.setCategoryPropertyOrder("client.color." + this.name, ImmutableList.of("red", "green", "blue"));
                config.setCategoryLanguageKey("client.color." + this.name, DamageIndicator.MOD_ID + ".category." + this.name);

                ConfigCategory category = config.getCategory("client.color." + this.name);
                Property red = category.get("red");
                red.setMaxValue(255);
                red.setMinValue(0);
                red.setHasSlidingControl(true);
                this.red = red.getInt(255);

                Property green = category.get("green");
                green.setMaxValue(255);
                green.setMinValue(0);
                green.setHasSlidingControl(true);
                this.green = green.getInt(255);

                Property blue = category.get("blue");
                blue.setMaxValue(255);
                blue.setMinValue(0);
                blue.setHasSlidingControl(true);
                this.blue = blue.getInt(255);

                if (config.hasCategory("color." + this.name)) {
                    ConfigCategory old = config.getCategory("color." + this.name);
                    config.removeCategory(old);
                    DamageIndicator.LOGGER.info("Removed the old config category: color.{}", this.name);

                    red.setValue(old.get("red").getInt(this.red));
                    green.setValue(old.get("green").getInt(this.green));
                    blue.setValue(old.get("blue").getInt(this.blue));
                    this.sync(config);
                }
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }

                if (o == null || getClass() != o.getClass()) {
                    return false;
                }

                return this.name.equals(((RGB) o).name);
            }

            @Override
            public int hashCode() {
                return Objects.hash(this.name);
            }
        }
    }
}
