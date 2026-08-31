package com.chatbubbles;

import com.chatbubbles.client.ChatBubblesClient;
import com.chatbubbles.config.ChatBubblesConfig;
import com.chatbubbles.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ChatBubbles.MOD_ID)
public class ChatBubbles {
    public static final String MOD_ID = "chatbubbles";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChatBubbles() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ChatBubblesConfig.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ChatBubblesConfig.CLIENT_SPEC);
        modBus.addListener(this::commonSetup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ChatBubblesClient.init(modBus));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
        LOGGER.info("Chat Bubbles ready for Minecraft 1.20.1 Forge");
    }
}
