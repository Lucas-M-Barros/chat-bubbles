package com.chatbubbles.network.packet;

import com.chatbubbles.client.ClientPacketHandler;
import com.chatbubbles.data.BubbleAppearance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncBubblePacket {
    private final UUID playerId;
    private final String message;
    private final long expireAt;
    private final BubbleAppearance appearance;

    public SyncBubblePacket(UUID playerId, String message, long expireAt, BubbleAppearance appearance) {
        this.playerId = playerId;
        this.message = message;
        this.expireAt = expireAt;
        this.appearance = appearance;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeUtf(message, 256);
        buf.writeLong(expireAt);
        appearance.encode(buf);
    }

    public static SyncBubblePacket decode(FriendlyByteBuf buf) {
        return new SyncBubblePacket(buf.readUUID(), buf.readUtf(256), buf.readLong(), BubbleAppearance.decode(buf));
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandler.handleSyncBubble(playerId, message, expireAt, appearance)));
        ctx.setPacketHandled(true);
    }
}
