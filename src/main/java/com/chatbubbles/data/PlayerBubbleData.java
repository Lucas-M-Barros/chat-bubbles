package com.chatbubbles.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerBubbleData {
    private static final String ROOT = "ChatBubbles";

    private PlayerBubbleData() {
    }

    public static BubbleAppearance get(ServerPlayer player) {
        return BubbleAppearance.load(getTag(player));
    }

    public static void set(ServerPlayer player, BubbleAppearance appearance) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ROOT, appearance.save());
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static CompoundTag getTag(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        return persisted.getCompound(ROOT);
    }
}
