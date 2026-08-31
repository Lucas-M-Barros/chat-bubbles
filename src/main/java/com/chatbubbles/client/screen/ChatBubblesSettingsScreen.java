package com.chatbubbles.client.screen;

import com.chatbubbles.client.BubbleRenderer;
import com.chatbubbles.client.ClientBubbleManager;
import com.chatbubbles.data.BubbleAppearance;
import com.chatbubbles.network.NetworkHandler;
import com.chatbubbles.network.packet.UpdateAppearancePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.UUID;
import java.util.function.IntConsumer;

public class ChatBubblesSettingsScreen extends Screen {
    private static final int ROW = 24;
    private static final int FIELD_WIDTH = 90;
    private static final int SLIDER_WIDTH = 200;

    private final BubbleAppearance appearance;
    private EditBox bgColorBox;
    private EditBox borderColorBox;
    private EditBox textColorBox;
    private OffsetSlider offsetSlider;
    private OffsetSlider spacingSlider;
    private CycleButton<Boolean> hideNametagButton;
    private int formY;

    public ChatBubblesSettingsScreen() {
        super(Component.translatable("chatbubbles.screen.title"));
        Minecraft minecraft = Minecraft.getInstance();
        UUID playerId = minecraft.player != null ? minecraft.player.getUUID() : new UUID(0L, 0L);
        this.appearance = ClientBubbleManager.getAppearance(playerId).copy();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.formY = 96;

        this.bgColorBox = colorBox(centerX + 10, this.formY, BubbleAppearance.toHex(this.appearance.bgColor()));
        this.borderColorBox = colorBox(centerX + 10, this.formY + ROW, BubbleAppearance.toHex(this.appearance.borderColor()));
        this.textColorBox = colorBox(centerX + 10, this.formY + ROW * 2, BubbleAppearance.toHex(this.appearance.textColor()));

        this.addRenderableWidget(this.bgColorBox);
        this.addRenderableWidget(this.borderColorBox);
        this.addRenderableWidget(this.textColorBox);

        int sliderY = this.formY + ROW * 3 + 6;
        this.offsetSlider = this.addRenderableWidget(new OffsetSlider(centerX - SLIDER_WIDTH / 2, sliderY, SLIDER_WIDTH, 20,
                "chatbubbles.option.offset", -2.0D, 8.0D, this.appearance.offset()));
        this.spacingSlider = this.addRenderableWidget(new OffsetSlider(centerX - SLIDER_WIDTH / 2, sliderY + ROW, SLIDER_WIDTH, 20,
                "chatbubbles.option.spacing", 1.0D, 10.0D, this.appearance.spacing()));

        this.hideNametagButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.appearance.hideNametag())
                .create(centerX - SLIDER_WIDTH / 2, sliderY + ROW * 2, SLIDER_WIDTH, 20,
                        Component.translatable("chatbubbles.option.hide_nametag"),
                        (button, value) -> this.appearance.setHideNametag(value)));

        int buttonY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.translatable("chatbubbles.screen.save"), button -> this.save())
                .bounds(centerX - 154, buttonY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("chatbubbles.screen.reset"), button -> this.reset())
                .bounds(centerX - 50, buttonY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(centerX + 54, buttonY, 100, 20).build());
    }

    private EditBox colorBox(int x, int y, String value) {
        EditBox box = new EditBox(this.font, x, y, FIELD_WIDTH, 20, Component.empty());
        box.setMaxLength(6);
        box.setValue(value);
        box.setFilter(text -> text.matches("[0-9a-fA-F]{0,6}"));
        return box;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("chatbubbles.screen.preview"), this.width / 2, 26, 0xAAAAAA);

        syncAppearanceFromWidgets();
        BubbleRenderer.drawPreview(graphics.pose(), this.width / 2, 52, this.appearance, Component.translatable("chatbubbles.screen.sample").getString());

        int labelX = this.width / 2 - 100;
        graphics.drawString(this.font, Component.translatable("chatbubbles.option.bg_color"), labelX, this.formY + 6, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("chatbubbles.option.border_color"), labelX, this.formY + ROW + 6, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("chatbubbles.option.text_color"), labelX, this.formY + ROW * 2 + 6, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void syncAppearanceFromWidgets() {
        applyColor(this.bgColorBox, this.appearance::setBgColor);
        applyColor(this.borderColorBox, this.appearance::setBorderColor);
        applyColor(this.textColorBox, this.appearance::setTextColor);
        if (this.offsetSlider != null) {
            this.appearance.setOffset((float) this.offsetSlider.actualValue());
        }
        if (this.spacingSlider != null) {
            this.appearance.setSpacing((float) this.spacingSlider.actualValue());
        }
    }

    private void applyColor(EditBox box, IntConsumer setter) {
        if (box == null) {
            return;
        }
        String value = box.getValue();
        if (value.length() == 6) {
            try {
                setter.accept(BubbleAppearance.parseHex(value));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void save() {
        syncAppearanceFromWidgets();
        NetworkHandler.CHANNEL.sendToServer(new UpdateAppearancePacket(this.appearance));
        if (this.minecraft != null && this.minecraft.player != null) {
            ClientBubbleManager.setAppearance(this.minecraft.player.getUUID(), this.appearance);
            this.minecraft.player.displayClientMessage(Component.translatable("chatbubbles.screen.saved"), true);
        }
        this.onClose();
    }

    private void reset() {
        this.appearance.setBgColor(BubbleAppearance.DEFAULT_BG);
        this.appearance.setBorderColor(BubbleAppearance.DEFAULT_BORDER);
        this.appearance.setTextColor(BubbleAppearance.DEFAULT_TEXT);
        this.appearance.setOffset(ClientBubbleManager.defaultOffset);
        this.appearance.setSpacing(ClientBubbleManager.defaultSpacing);
        this.appearance.setHideNametag(ClientBubbleManager.defaultHideNametag);
        this.rebuildWidgets();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class OffsetSlider extends AbstractSliderButton {
        private final String langKey;
        private final double min;
        private final double max;

        OffsetSlider(int x, int y, int width, int height, String langKey, double min, double max, double current) {
            super(x, y, width, height, Component.empty(), 0.0D);
            this.langKey = langKey;
            this.min = min;
            this.max = max;
            this.value = Mth.clamp((current - min) / (max - min), 0.0D, 1.0D);
            this.updateMessage();
        }

        double actualValue() {
            return this.min + this.value * (this.max - this.min);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable(this.langKey, String.format("%.2f", this.actualValue())));
        }

        @Override
        protected void applyValue() {
        }
    }
}
