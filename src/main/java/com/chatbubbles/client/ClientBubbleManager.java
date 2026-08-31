package com.chatbubbles.client;

import com.chatbubbles.data.BubbleAppearance;
import com.chatbubbles.data.ChatBubble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClientBubbleManager {
    private static final Map<UUID, List<ChatBubble>> BUBBLES = new HashMap<>();
    private static final Map<UUID, BubbleAppearance> APPEARANCES = new HashMap<>();

    public static int maxBubbles = 3;
    public static int padding = 4;
    public static int maxLineWidth = 160;
    public static float defaultOffset = 2.0F;
    public static float defaultSpacing = 1.0F;
    public static boolean defaultHideNametag;

    private ClientBubbleManager() {
    }

    public static void addBubble(UUID playerId, String message, long expireAt, BubbleAppearance appearance) {
        APPEARANCES.put(playerId, appearance.copy());
        List<ChatBubble> list = BUBBLES.computeIfAbsent(playerId, unused -> new ArrayList<>());
        list.add(0, new ChatBubble(message, expireAt));
        while (list.size() > maxBubbles) {
            list.remove(list.size() - 1);
        }
    }

    public static List<ChatBubble> getBubbles(UUID playerId) {
        List<ChatBubble> list = BUBBLES.get(playerId);
        if (list == null) {
            return List.of();
        }
        return list;
    }

    public static BubbleAppearance getAppearance(UUID playerId) {
        BubbleAppearance stored = APPEARANCES.get(playerId);
        if (stored != null) {
            return stored;
        }
        BubbleAppearance fallback = new BubbleAppearance();
        fallback.setOffset(defaultOffset);
        fallback.setSpacing(defaultSpacing);
        fallback.setHideNametag(defaultHideNametag);
        return fallback;
    }

    public static void setAppearance(UUID playerId, BubbleAppearance appearance) {
        APPEARANCES.put(playerId, appearance.copy());
    }

    public static boolean shouldHideNametag(UUID playerId) {
        if (getBubbles(playerId).isEmpty()) {
            return false;
        }
        return getAppearance(playerId).hideNametag();
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        BUBBLES.values().forEach(list -> list.removeIf(bubble -> bubble.expireAt() <= now));
        BUBBLES.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public static void clear() {
        BUBBLES.clear();
        APPEARANCES.clear();
    }

    public static void applyServerSettings(int maxBubbles, int padding, int maxLineWidth, float defaultOffset, float defaultSpacing, boolean defaultHideNametag) {
        ClientBubbleManager.maxBubbles = maxBubbles;
        ClientBubbleManager.padding = padding;
        ClientBubbleManager.maxLineWidth = maxLineWidth;
        ClientBubbleManager.defaultOffset = defaultOffset;
        ClientBubbleManager.defaultSpacing = defaultSpacing;
        ClientBubbleManager.defaultHideNametag = defaultHideNametag;
    }
}
