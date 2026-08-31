package com.chatbubbles.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class ChatBubblesConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Server, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();

        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    private ChatBubblesConfig() {
    }

    public static final class Server {
        public final ForgeConfigSpec.IntValue maxBubbles;
        public final ForgeConfigSpec.IntValue durationSeconds;
        public final ForgeConfigSpec.IntValue padding;
        public final ForgeConfigSpec.IntValue maxLineWidth;
        public final ForgeConfigSpec.DoubleValue defaultOffset;
        public final ForgeConfigSpec.DoubleValue defaultSpacing;
        public final ForgeConfigSpec.BooleanValue defaultHideNametag;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Server settings for Chat Bubbles").push("server");

            maxBubbles = builder
                    .comment("Maximum number of bubbles stacked above a player's head")
                    .defineInRange("maxBubbles", 3, 1, 8);

            durationSeconds = builder
                    .comment("How long a bubble stays visible, in seconds")
                    .defineInRange("durationSeconds", 8, 1, 60);

            padding = builder
                    .comment("Inner padding of the bubble, in pixels")
                    .defineInRange("padding", 4, 1, 16);

            maxLineWidth = builder
                    .comment("Maximum text width before wrapping, in pixels")
                    .defineInRange("maxLineWidth", 160, 40, 320);

            defaultOffset = builder
                    .comment("Default height offset above the player's head. 2.0+ is recommended")
                    .defineInRange("defaultOffset", 2.0D, -2.0D, 8.0D);

            defaultSpacing = builder
                    .comment("Default spacing between stacked bubbles")
                    .defineInRange("defaultSpacing", 1.0D, 1.0D, 10.0D);

            defaultHideNametag = builder
                    .comment("Whether nametags are hidden by default while a bubble is visible")
                    .define("defaultHideNametag", false);

            builder.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue enabled;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client settings for Chat Bubbles").push("client");
            enabled = builder
                    .comment("Show chat bubbles above players")
                    .define("enabled", true);
            builder.pop();
        }
    }
}
