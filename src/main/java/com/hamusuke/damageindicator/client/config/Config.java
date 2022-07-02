package com.hamusuke.damageindicator.client.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.List;

public class Config {
    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CONFIG;

    static {
        Pair<ClientConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = pair.getLeft();
        CONFIG = pair.getRight();
    }

    public static class ClientConfig {
        public final ForgeConfigSpec.BooleanValue hideIndicator;
        public final ForgeConfigSpec.BooleanValue forceIndicatorRendering;
        public final ForgeConfigSpec.DoubleValue fontSize;
        public final ForgeConfigSpec.IntValue renderDistance;
        public final ForgeConfigSpec.BooleanValue changeColorWhenCrit;
        public final ColorConfig colorConfig;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("client");
            this.hideIndicator = builder.define("hide_indicator", false);
            this.forceIndicatorRendering = builder.define("force_indicator_rendering", false);
            this.fontSize = builder.defineInRange("font_size", 1.0D, 0.0D, 1.0D);
            this.renderDistance = builder.defineInRange("render_distance", 64, 0, 1024);
            this.changeColorWhenCrit = builder.define("change_color_when_crit", false);
            this.colorConfig = new ColorConfig(builder);
            builder.pop();
        }

        public static class ColorConfig {
            private final List<RGB> colorConfigs = Lists.newArrayList();
            public final RGB inFireDamage;
            public final RGB lightningBoltDamage;
            public final RGB onFireDamage;
            public final RGB lavaDamage;
            public final RGB hotFloorDamage;
            public final RGB inWallDamage;
            public final RGB crammingDamage;
            public final RGB drownDamage;
            public final RGB starveDamage;
            public final RGB cactusDamage;
            public final RGB fallDamage;
            public final RGB flyIntoWallDamage;
            public final RGB outOfWorldDamage;
            public final RGB genericDamage;
            public final RGB magicDamage;
            public final RGB witherDamage;
            public final RGB anvilDamage;
            public final RGB fallingBlockDamage;
            public final RGB dragonBreathDamage;
            public final RGB dryOutDamage;
            public final RGB sweetBerryBushDamage;
            public final RGB critical;
            public final RGB heal;
            public final RGB immune;

            private RGB register(ForgeConfigSpec.Builder builder, String path) {
                return this.register(builder, path, 255, 255, 255);
            }

            private RGB register(ForgeConfigSpec.Builder builder, String path, int red, int green, int blue) {
                return this.register(new RGB(builder, path, red, green, blue));
            }

            private RGB register(RGB rgb) {
                this.colorConfigs.add(rgb);
                return rgb;
            }

            public int getRGBFromDamageSource(String source) {
                for (RGB rgb : this.colorConfigs) {
                    if (rgb.path.replace("_", "").replace("damage", "").equalsIgnoreCase(source)) {
                        return rgb.toRGB();
                    }
                }

                return 16777215;
            }

            public ImmutableList<RGB> immutable() {
                return ImmutableList.copyOf(this.colorConfigs);
            }

            public ColorConfig(ForgeConfigSpec.Builder builder) {
                builder.push("color");
                this.inFireDamage = this.register(builder, "in_fire_damage", 255, 150, 0);
                this.lightningBoltDamage = this.register(builder, "lightning_bolt_damage", 255, 80, 255);
                this.onFireDamage = this.register(builder, "on_fire_damage", 255, 150, 0);
                this.lavaDamage = this.register(builder, "lava_damage", 255, 150, 0);
                this.hotFloorDamage = this.register(builder, "hot_floor_damage", 255, 150, 0);
                this.inWallDamage = this.register(builder, "in_wall_damage", 255, 225, 0);
                this.crammingDamage = this.register(builder, "cramming_damage", 255, 225, 0);
                this.drownDamage = this.register(builder, "drown_damage", 0, 20, 255);
                this.starveDamage = this.register(builder, "starve_damage", 150, 100, 0);
                this.cactusDamage = this.register(builder, "cactus_damage", 0, 255, 0);
                this.fallDamage = this.register(builder, "fall_damage", 255, 225, 0);
                this.flyIntoWallDamage = this.register(builder, "fly_into_wall_damage", 255, 225, 0);
                this.outOfWorldDamage = this.register(builder, "out_of_world_damage", 0, 0, 0);
                this.genericDamage = this.register(builder, "generic_damage");
                this.magicDamage = this.register(builder, "magic_damage", 0, 255, 160);
                this.witherDamage = this.register(builder, "wither_damage", 25, 25, 25);
                this.anvilDamage = this.register(builder, "anvil_damage", 255, 225, 0);
                this.fallingBlockDamage = this.register(builder, "falling_block_damage", 255, 225, 0);
                this.dragonBreathDamage = this.register(builder, "dragon_breath_damage");
                this.dryOutDamage = this.register(builder, "dry_out_damage");
                this.sweetBerryBushDamage = this.register(builder, "sweet_berry_bush_damage");
                this.critical = this.register(builder, "critical", 255, 255, 0);
                this.heal = register(builder, "heal", 85, 255, 85);
                this.immune = register(builder, "immune", 170, 170, 170);
                builder.pop();
            }

            public static class RGB {
                public final String path;
                public final ForgeConfigSpec.IntValue red;
                public final ForgeConfigSpec.IntValue green;
                public final ForgeConfigSpec.IntValue blue;

                RGB(ForgeConfigSpec.Builder builder, String path, int red, int green, int blue) {
                    this.path = path;
                    builder.push(this.path);
                    this.red = builder.defineInRange("red", red, 0, 255);
                    this.green = builder.defineInRange("green", green, 0, 255);
                    this.blue = builder.defineInRange("blue", blue, 0, 255);
                    builder.pop();
                }

                public int toRGB() {
                    return new Color(this.red.get(), this.green.get(), this.blue.get()).getRGB();
                }
            }
        }
    }
}
