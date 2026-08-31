package com.chatbubbles.server;

import com.chatbubbles.ChatBubbles;
import com.chatbubbles.config.ChatBubblesConfig;
import com.chatbubbles.data.BubbleAppearance;
import com.chatbubbles.data.PlayerBubbleData;
import com.chatbubbles.network.NetworkHandler;
import com.chatbubbles.network.packet.SyncAppearancePacket;
import com.chatbubbles.network.packet.SyncBubblePacket;
import com.chatbubbles.network.packet.SyncServerSettingsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ChatBubbles.MOD_ID)
public final class ServerEvents {
    private ServerEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ChatBubblesCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SyncServerSettingsPacket.fromConfig());

        BubbleAppearance self = PlayerBubbleData.get(player);
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncAppearancePacket(player.getUUID(), self));

        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            if (other == player) {
                continue;
            }
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncAppearancePacket(other.getUUID(), PlayerBubbleData.get(other))
            );
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> other),
                    new SyncAppearancePacket(player.getUUID(), self)
            );
        }
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getRawText();
        if (message == null || message.isBlank()) {
            return;
        }
        if (message.length() > 256) {
            message = message.substring(0, 256);
        }

        BubbleAppearance appearance = PlayerBubbleData.get(player);
        long expireAt = System.currentTimeMillis() + ChatBubblesConfig.SERVER.durationSeconds.get() * 1000L;

        NetworkHandler.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncBubblePacket(player.getUUID(), message, expireAt, appearance)
        );
    }
}
