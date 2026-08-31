package com.chatbubbles.data;

public record ChatBubble(String message, long expireAt) {
    public float alpha() {
        long remaining = expireAt - System.currentTimeMillis();
        if (remaining <= 0L) {
            return 0.0F;
        }
        if (remaining < 1000L) {
            return remaining / 1000.0F;
        }
        return 1.0F;
    }

    public boolean expired() {
        return expireAt <= System.currentTimeMillis();
    }
}
