package com.chatbubbles.network;

import com.chatbubbles.ChatBubbles;
import com.chatbubbles.network.packet.OpenSettingsPacket;
import com.chatbubbles.network.packet.SyncAppearancePacket;
import com.chatbubbles.network.packet.SyncBubblePacket;
import com.chatbubbles.network.packet.SyncServerSettingsPacket;
import com.chatbubbles.network.packet.UpdateAppearancePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ChatBubbles.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private NetworkHandler() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncBubblePacket.class, SyncBubblePacket::encode, SyncBubblePacket::decode, SyncBubblePacket::handle);
        CHANNEL.registerMessage(id++, UpdateAppearancePacket.class, UpdateAppearancePacket::encode, UpdateAppearancePacket::decode, UpdateAppearancePacket::handle);
        CHANNEL.registerMessage(id++, SyncAppearancePacket.class, SyncAppearancePacket::encode, SyncAppearancePacket::decode, SyncAppearancePacket::handle);
        CHANNEL.registerMessage(id++, SyncServerSettingsPacket.class, SyncServerSettingsPacket::encode, SyncServerSettingsPacket::decode, SyncServerSettingsPacket::handle);
        CHANNEL.registerMessage(id++, OpenSettingsPacket.class, OpenSettingsPacket::encode, OpenSettingsPacket::decode, OpenSettingsPacket::handle);
    }
}
