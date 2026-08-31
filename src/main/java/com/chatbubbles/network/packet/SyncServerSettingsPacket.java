package com.chatbubbles.network.packet;

import com.chatbubbles.client.ClientPacketHandler;
import com.chatbubbles.config.ChatBubblesConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncServerSettingsPacket {
    private final int maxBubbles;
    private final int padding;
    private final int maxLineWidth;
    private final float defaultOffset;
    private final float defaultSpacing;
    private final boolean defaultHideNametag;

    public SyncServerSettingsPacket(int maxBubbles, int padding, int maxLineWidth, float defaultOffset, float defaultSpacing, boolean defaultHideNametag) {
        this.maxBubbles = maxBubbles;
        this.padding = padding;
        this.maxLineWidth = maxLineWidth;
        this.defaultOffset = defaultOffset;
        this.defaultSpacing = defaultSpacing;
        this.defaultHideNametag = defaultHideNametag;
    }

    public static SyncServerSettingsPacket fromConfig() {
        return new SyncServerSettingsPacket(
                ChatBubblesConfig.SERVER.maxBubbles.get(),
                ChatBubblesConfig.SERVER.padding.get(),
                ChatBubblesConfig.SERVER.maxLineWidth.get(),
                ChatBubblesConfig.SERVER.defaultOffset.get().floatValue(),
                ChatBubblesConfig.SERVER.defaultSpacing.get().floatValue(),
                ChatBubblesConfig.SERVER.defaultHideNametag.get()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(maxBubbles);
        buf.writeVarInt(padding);
        buf.writeVarInt(maxLineWidth);
        buf.writeFloat(defaultOffset);
        buf.writeFloat(defaultSpacing);
        buf.writeBoolean(defaultHideNametag);
    }

    public static SyncServerSettingsPacket decode(FriendlyByteBuf buf) {
        return new SyncServerSettingsPacket(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readBoolean()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandler.handleServerSettings(maxBubbles, padding, maxLineWidth, defaultOffset, defaultSpacing, defaultHideNametag)));
        ctx.setPacketHandled(true);
    }
}
