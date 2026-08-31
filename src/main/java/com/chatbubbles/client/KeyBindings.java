package com.chatbubbles.client;

import com.chatbubbles.ChatBubbles;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    public static final KeyMapping OPEN_SETTINGS = new KeyMapping(
            "key.chatbubbles.settings",
            GLFW.GLFW_KEY_C,
            "key.categories.chatbubbles"
    );

    private KeyBindings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SETTINGS);
    }
}
