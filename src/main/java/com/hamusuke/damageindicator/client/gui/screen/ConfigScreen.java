package com.hamusuke.damageindicator.client.gui.screen;

import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.client.config.Config;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.widget.Slider;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ConfigScreen extends Screen {
    private static final ITextComponent HIDE_INDICATOR = new TranslationTextComponent("options.damageindicator.hideIndicator");
    private static final ITextComponent FORCE_INDICATOR_RENDERING = new TranslationTextComponent("options.damageindicator.forceindicatorrendering");
    private static final ITextComponent CHANGE_COLOR_WHEN_CRIT = new TranslationTextComponent("options.damageindicator.changeColorWhenCrit");
    private final Screen parent;

    public ConfigScreen(Minecraft ignored, Screen parent) {
        super(new TranslationTextComponent("options.title"));
        this.parent = parent;
    }

    private static ITextComponent percent(String key, String value) {
        return new TranslationTextComponent("options.percent_value", key, value);
    }

    @Override
    protected void init() {
        this.addButton(new Button(this.width / 4, this.height / 2 - 60, this.width / 2, 20, DialogTexts.optionStatus(HIDE_INDICATOR, Config.CLIENT.hideIndicator.get()), p_onPress_1_ -> {
            Config.CLIENT.hideIndicator.set(!Config.CLIENT.hideIndicator.get());
            p_onPress_1_.setMessage(DialogTexts.optionStatus(HIDE_INDICATOR, Config.CLIENT.hideIndicator.get()));
        }));

        this.addButton(new Button(this.width / 4, this.height / 2 - 40, this.width / 2, 20, DialogTexts.optionStatus(FORCE_INDICATOR_RENDERING, Config.CLIENT.forceIndicatorRendering.get()), p_onPress_1_ -> {
            Config.CLIENT.forceIndicatorRendering.set(!Config.CLIENT.forceIndicatorRendering.get());
            p_onPress_1_.setMessage(DialogTexts.optionStatus(FORCE_INDICATOR_RENDERING, Config.CLIENT.forceIndicatorRendering.get()));
        }));

        this.addButton(new AbstractSlider(this.width / 4, this.height / 2 - 20, this.width / 2, 20, new TranslationTextComponent("options.damageindicator.displayDistance").append(": ").append("" + Config.CLIENT.renderDistance.get()), (double) Config.CLIENT.renderDistance.get() / 1024.0D) {
            @Override
            protected void updateMessage() {
                this.setMessage(new TranslationTextComponent("options.damageindicator.displayDistance").append(": ").append("" + Config.CLIENT.renderDistance.get()));
            }

            @Override
            protected void applyValue() {
                Config.CLIENT.renderDistance.set(MathHelper.clamp((int) (this.value * 1024.0D), 0, 1024));
            }
        });

        this.addButton(new AbstractSlider(this.width / 4, this.height / 2, this.width / 2, 20, percent("options.damageindicator.fontSize", String.format("%.1f", Config.CLIENT.fontSize.get() * 100.0D)), Config.CLIENT.fontSize.get()) {
            @Override
            protected void updateMessage() {
                this.setMessage(percent("options.damageindicator.fontSize", String.format("%.1f", Config.CLIENT.fontSize.get() * 100.0D)));
            }

            @Override
            protected void applyValue() {
                Config.CLIENT.fontSize.set(MathHelper.clamp(this.value, 0.0D, 1.0D));
            }
        });

        this.addButton(new Button(this.width / 4, this.height / 2 + 20, this.width / 2, 20, DialogTexts.optionStatus(CHANGE_COLOR_WHEN_CRIT, Config.CLIENT.changeColorWhenCrit.get()), p_onPress_1_ -> {
            Config.CLIENT.changeColorWhenCrit.set(!Config.CLIENT.changeColorWhenCrit.get());
            p_onPress_1_.setMessage(DialogTexts.optionStatus(CHANGE_COLOR_WHEN_CRIT, Config.CLIENT.changeColorWhenCrit.get()));
        }));

        this.addButton(new Button(this.width / 4, this.height / 2 + 40, this.width / 2, 20, new TranslationTextComponent(DamageIndicator.MOD_ID + ".config.colorConfig.title"), p_onPress_1_ -> this.minecraft.setScreen(new ColorSettingsScreen(this))));

        this.addButton(new Button(this.width / 4, this.height - 20, this.width / 2, 20, DialogTexts.GUI_DONE, p_onPress_1_ -> this.onClose()));
    }

    @Override
    public void render(MatrixStack p_96562_, int p_96563_, int p_96564_, float p_96565_) {
        this.renderBackground(p_96562_);
        drawCenteredString(p_96562_, this.font, this.getTitle(), this.width / 2, 10, 16777215);
        super.render(p_96562_, p_96563_, p_96564_, p_96565_);
    }

    @Override
    public void removed() {
        Config.CONFIG.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @OnlyIn(Dist.CLIENT)
    private static class ColorSettingsScreen extends Screen {
        @Nullable
        private final Screen parent;
        private ColorList list;

        private ColorSettingsScreen(@Nullable Screen parent) {
            super(new TranslationTextComponent(DamageIndicator.MOD_ID + ".config.colorConfig.title"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            super.init();
            double amount = this.list != null ? this.list.getScrollAmount() : 0.0D;
            this.list = new ColorList();
            this.list.setScrollAmount(amount);
            this.addWidget(this.list);
            this.addButton(new Button(this.width / 2 - this.width / 4, this.height - 20, this.width / 2, 20, DialogTexts.GUI_DONE, p_onPress_1_ -> this.onClose()));
        }

        @Override
        public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
            this.list.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
            super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(this.parent);
        }

        @OnlyIn(Dist.CLIENT)
        private class ColorList extends ExtendedList<ColorList.Color> {
            public ColorList() {
                super(ColorSettingsScreen.this.minecraft, ColorSettingsScreen.this.width, ColorSettingsScreen.this.height, 20, ColorSettingsScreen.this.height - 20, 20);
                for (Config.ClientConfig.ColorConfig.RGB rgb : Config.CLIENT.colorConfig.immutable()) {
                    this.addEntry(new Color(rgb));
                }
            }

            @OnlyIn(Dist.CLIENT)
            private class Color extends ExtendedList.AbstractListEntry<Color> {
                private final Button button;

                private Color(Config.ClientConfig.ColorConfig.RGB rgbConfig) {
                    this.button = new Button(ColorSettingsScreen.this.width / 4, 0, ColorSettingsScreen.this.width / 2, 20, new TranslationTextComponent(DamageIndicator.MOD_ID + ".config.color." + rgbConfig.path), p_onPress_1_ -> ColorSettingsScreen.this.minecraft.setScreen(new ColorMixingScreen(ColorSettingsScreen.this, rgbConfig)));
                }

                @Override
                public void render(MatrixStack p_230432_1_, int p_230432_2_, int p_230432_3_, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_) {
                    this.button.y = p_230432_3_;
                    this.button.render(p_230432_1_, p_230432_7_, p_230432_8_, p_230432_10_);
                }

                @Override
                public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
                    return this.button.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
                }
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static class ColorMixingScreen extends Screen {
            @Nullable
            private final Screen parent;
            private final Config.ClientConfig.ColorConfig.RGB rgb;
            private Slider red;
            private Slider green;
            private Slider blue;

            private ColorMixingScreen(@Nullable Screen parent, Config.ClientConfig.ColorConfig.RGB rgb) {
                super(new TranslationTextComponent(DamageIndicator.MOD_ID + ".config.color." + rgb.path));
                this.parent = parent;
                this.rgb = rgb;
            }

            @Override
            protected void init() {
                super.init();

                this.red = this.addButton(new Slider(this.width / 4, this.height / 2 - 70, this.width / 2, 20, ITextComponent.nullToEmpty("Red: "), StringTextComponent.EMPTY, 0.0D, 255.0D, this.rgb.red.get(), false, true, p_onPress_1_ -> {
                }));
                this.green = this.addButton(new Slider(this.width / 4, this.height / 2 - 45, this.width / 2, 20, ITextComponent.nullToEmpty("Green: "), StringTextComponent.EMPTY, 0.0D, 255.0D, this.rgb.green.get(), false, true, p_onPress_1_ -> {
                }));
                this.blue = this.addButton(new Slider(this.width / 4, this.height / 2 - 20, this.width / 2, 20, ITextComponent.nullToEmpty("Blue: "), StringTextComponent.EMPTY, 0.0D, 255.0D, this.rgb.blue.get(), false, true, p_onPress_1_ -> {
                }));
                this.addButton(new Button(this.width / 2 - this.width / 4, this.height - 20, this.width / 2, 20, DialogTexts.GUI_DONE, p_onPress_1_ -> this.onClose()));
            }

            @Override
            public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
                this.renderBackground(p_230430_1_);
                drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, 5, 16777215);
                int color = MathHelper.color(this.red.getValueInt(), this.green.getValueInt(), this.blue.getValueInt()) + (255 << 24);
                this.fillGradient(p_230430_1_, this.width / 4, this.height / 2 + 5, this.width * 3 / 4, this.height - 25, color, color);
                super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
            }

            @Override
            public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
                MutableBoolean mutableBoolean = new MutableBoolean();
                this.children().forEach(iGuiEventListener -> {
                    boolean bl = iGuiEventListener.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_);
                    if (!mutableBoolean.booleanValue() && bl) {
                        mutableBoolean.setValue(true);
                    }
                });
                return mutableBoolean.booleanValue() || super.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_);
            }

            @Override
            public void removed() {
                super.removed();
                this.rgb.red.set(this.red.getValueInt());
                this.rgb.green.set(this.green.getValueInt());
                this.rgb.blue.set(this.blue.getValueInt());
                Config.CONFIG.save();
            }

            @Override
            public void onClose() {
                this.minecraft.setScreen(this.parent);
            }
        }
    }
}
