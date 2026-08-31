package com.chatbubbles.client;

import com.chatbubbles.client.screen.ChatBubblesSettingsScreen;
import com.chatbubbles.data.BubbleAppearance;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void handleSyncBubble(UUID playerId, String message, long expireAt, BubbleAppearance appearance) {
        ClientBubbleManager.addBubble(playerId, message, expireAt, appearance);
    }

    public static void handleSyncAppearance(UUID playerId, BubbleAppearance appearance) {
        ClientBubbleManager.setAppearance(playerId, appearance);
    }

    public static void handleServerSettings(int maxBubbles, int padding, int maxLineWidth, float defaultOffset, float defaultSpacing, boolean defaultHideNametag) {
        ClientBubbleManager.applyServerSettings(maxBubbles, padding, maxLineWidth, defaultOffset, defaultSpacing, defaultHideNametag);
    }

    public static void handleOpenSettings() {
        Minecraft.getInstance().setScreen(new ChatBubblesSettingsScreen());
    }
}
