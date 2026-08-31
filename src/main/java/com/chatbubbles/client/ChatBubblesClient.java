package com.chatbubbles.client;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;

public final class ChatBubblesClient {
    private ChatBubblesClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(KeyBindings::register);
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
    }
}
