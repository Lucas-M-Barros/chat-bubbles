package com.chatbubbles.data;

import com.chatbubbles.config.ChatBubblesConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public final class BubbleAppearance {
    public static final int DEFAULT_BG = 0xFFFFFF;
    public static final int DEFAULT_BORDER = 0x2B2B2B;
    public static final int DEFAULT_TEXT = 0x1A1A1A;

    private int bgColor = DEFAULT_BG;
    private int borderColor = DEFAULT_BORDER;
    private int textColor = DEFAULT_TEXT;
    private float offset = 2.0F;
    private float spacing = 1.0F;
    private boolean hideNametag;

    public static BubbleAppearance defaults() {
        BubbleAppearance appearance = new BubbleAppearance();
        if (ChatBubblesConfig.SERVER_SPEC.isLoaded()) {
            appearance.offset = ChatBubblesConfig.SERVER.defaultOffset.get().floatValue();
            appearance.spacing = ChatBubblesConfig.SERVER.defaultSpacing.get().floatValue();
            appearance.hideNametag = ChatBubblesConfig.SERVER.defaultHideNametag.get();
        }
        return appearance;
    }

    public int bgColor() {
        return bgColor;
    }

    public int borderColor() {
        return borderColor;
    }

    public int textColor() {
        return textColor;
    }

    public float offset() {
        return offset;
    }

    public float spacing() {
        return spacing;
    }

    public boolean hideNametag() {
        return hideNametag;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor & 0xFFFFFF;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor & 0xFFFFFF;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor & 0xFFFFFF;
    }

    public void setOffset(float offset) {
        this.offset = Mth.clamp(offset, -2.0F, 8.0F);
    }

    public void setSpacing(float spacing) {
        this.spacing = Mth.clamp(spacing, 1.0F, 10.0F);
    }

    public void setHideNametag(boolean hideNametag) {
        this.hideNametag = hideNametag;
    }

    public void copyFrom(BubbleAppearance other) {
        this.bgColor = other.bgColor;
        this.borderColor = other.borderColor;
        this.textColor = other.textColor;
        this.offset = other.offset;
        this.spacing = other.spacing;
        this.hideNametag = other.hideNametag;
    }

    public BubbleAppearance copy() {
        BubbleAppearance copy = new BubbleAppearance();
        copy.copyFrom(this);
        return copy;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(bgColor);
        buf.writeInt(borderColor);
        buf.writeInt(textColor);
        buf.writeFloat(offset);
        buf.writeFloat(spacing);
        buf.writeBoolean(hideNametag);
    }

    public static BubbleAppearance decode(FriendlyByteBuf buf) {
        BubbleAppearance appearance = new BubbleAppearance();
        appearance.bgColor = buf.readInt() & 0xFFFFFF;
        appearance.borderColor = buf.readInt() & 0xFFFFFF;
        appearance.textColor = buf.readInt() & 0xFFFFFF;
        appearance.setOffset(buf.readFloat());
        appearance.setSpacing(buf.readFloat());
        appearance.hideNametag = buf.readBoolean();
        return appearance;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("bgColor", bgColor);
        tag.putInt("borderColor", borderColor);
        tag.putInt("textColor", textColor);
        tag.putFloat("offset", offset);
        tag.putFloat("spacing", spacing);
        tag.putBoolean("hideNametag", hideNametag);
        return tag;
    }

    public static BubbleAppearance load(CompoundTag tag) {
        BubbleAppearance appearance = defaults();
        if (tag.contains("bgColor")) {
            appearance.bgColor = tag.getInt("bgColor") & 0xFFFFFF;
        }
        if (tag.contains("borderColor")) {
            appearance.borderColor = tag.getInt("borderColor") & 0xFFFFFF;
        }
        if (tag.contains("textColor")) {
            appearance.textColor = tag.getInt("textColor") & 0xFFFFFF;
        }
        if (tag.contains("offset")) {
            appearance.setOffset(tag.getFloat("offset"));
        }
        if (tag.contains("spacing")) {
            appearance.setSpacing(tag.getFloat("spacing"));
        }
        if (tag.contains("hideNametag")) {
            appearance.hideNametag = tag.getBoolean("hideNametag");
        }
        return appearance;
    }

    public static int parseHex(String hex) {
        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
        if (!cleaned.matches("(?i)[0-9a-f]{6}")) {
            throw new IllegalArgumentException("Invalid hex color: " + hex);
        }
        return Integer.parseInt(cleaned, 16);
    }

    public static String toHex(int rgb) {
        return String.format("%06X", rgb & 0xFFFFFF);
    }
}
