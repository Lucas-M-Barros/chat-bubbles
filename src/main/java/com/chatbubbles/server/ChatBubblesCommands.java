package com.chatbubbles.server;

import com.chatbubbles.data.BubbleAppearance;
import com.chatbubbles.data.PlayerBubbleData;
import com.chatbubbles.network.NetworkHandler;
import com.chatbubbles.network.packet.OpenSettingsPacket;
import com.chatbubbles.network.packet.SyncAppearancePacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Consumer;

public final class ChatBubblesCommands {
    private static final SimpleCommandExceptionType INVALID_HEX =
            new SimpleCommandExceptionType(Component.translatable("chatbubbles.command.invalid_hex"));

    private ChatBubblesCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("chatbubbles")
                .then(Commands.literal("settings")
                        .executes(ctx -> openSettings(ctx.getSource())))
                .then(Commands.literal("bgColor")
                        .then(Commands.argument("hex", StringArgumentType.word())
                                .executes(ctx -> {
                                    int color = parseHex(StringArgumentType.getString(ctx, "hex"));
                                    return update(ctx.getSource(), appearance -> appearance.setBgColor(color),
                                            Component.translatable("chatbubbles.command.bg_color", BubbleAppearance.toHex(color)));
                                })))
                .then(Commands.literal("borderColor")
                        .then(Commands.argument("hex", StringArgumentType.word())
                                .executes(ctx -> {
                                    int color = parseHex(StringArgumentType.getString(ctx, "hex"));
                                    return update(ctx.getSource(), appearance -> appearance.setBorderColor(color),
                                            Component.translatable("chatbubbles.command.border_color", BubbleAppearance.toHex(color)));
                                })))
                .then(Commands.literal("textColor")
                        .then(Commands.argument("hex", StringArgumentType.word())
                                .executes(ctx -> {
                                    int color = parseHex(StringArgumentType.getString(ctx, "hex"));
                                    return update(ctx.getSource(), appearance -> appearance.setTextColor(color),
                                            Component.translatable("chatbubbles.command.text_color", BubbleAppearance.toHex(color)));
                                })))
                .then(Commands.literal("offset")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(-2.0F, 8.0F))
                                .executes(ctx -> update(ctx.getSource(), appearance -> {
                                    appearance.setOffset(FloatArgumentType.getFloat(ctx, "value"));
                                }, Component.translatable("chatbubbles.command.offset", FloatArgumentType.getFloat(ctx, "value"))))))
                .then(Commands.literal("spacing")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(1.0F, 10.0F))
                                .executes(ctx -> update(ctx.getSource(), appearance -> {
                                    appearance.setSpacing(FloatArgumentType.getFloat(ctx, "value"));
                                }, Component.translatable("chatbubbles.command.spacing", FloatArgumentType.getFloat(ctx, "value"))))))
                .then(Commands.literal("hideNametag")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> update(ctx.getSource(), appearance -> {
                                    appearance.setHideNametag(BoolArgumentType.getBool(ctx, "value"));
                                }, Component.translatable("chatbubbles.command.hide_nametag", BoolArgumentType.getBool(ctx, "value"))))))
        );
    }

    private static int openSettings(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenSettingsPacket());
        return 1;
    }

    private static int update(CommandSourceStack source, Consumer<BubbleAppearance> updater, Component feedback) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        BubbleAppearance appearance = PlayerBubbleData.get(player);
        updater.accept(appearance);
        PlayerBubbleData.set(player, appearance);
        NetworkHandler.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncAppearancePacket(player.getUUID(), appearance)
        );
        source.sendSuccess(() -> feedback, false);
        return 1;
    }

    private static int parseHex(String hex) throws CommandSyntaxException {
        try {
            return BubbleAppearance.parseHex(hex);
        } catch (IllegalArgumentException ex) {
            throw INVALID_HEX.create();
        }
    }
}
