package com.chatbubbles.client;

import com.chatbubbles.client.screen.ChatBubblesSettingsScreen;
import com.chatbubbles.config.ChatBubblesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientBubbleManager.tick();
        Minecraft minecraft = Minecraft.getInstance();
        while (KeyBindings.OPEN_SETTINGS.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new ChatBubblesSettingsScreen());
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        if (!ChatBubblesConfig.CLIENT.enabled.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.isInvisible() || player.isSpectator()) {
            return;
        }
        LocalPlayer self = Minecraft.getInstance().player;
        if (self != null && player == self && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return;
        }
        if (ClientBubbleManager.getBubbles(player.getUUID()).isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        BubbleRenderer.render(player, poseStack, buffer, event.getPackedLight());
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!ChatBubblesConfig.CLIENT.enabled.get()) {
            return;
        }
        if (event.getEntity() instanceof Player player && ClientBubbleManager.shouldHideNametag(player.getUUID())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientBubbleManager.clear();
    }
}
