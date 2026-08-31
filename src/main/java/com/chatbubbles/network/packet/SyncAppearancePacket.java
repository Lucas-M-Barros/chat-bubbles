package com.chatbubbles.network.packet;

import com.chatbubbles.client.ClientPacketHandler;
import com.chatbubbles.data.BubbleAppearance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncAppearancePacket {
    private final UUID playerId;
    private final BubbleAppearance appearance;

    public SyncAppearancePacket(UUID playerId, BubbleAppearance appearance) {
        this.playerId = playerId;
        this.appearance = appearance;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        appearance.encode(buf);
    }

    public static SyncAppearancePacket decode(FriendlyByteBuf buf) {
        return new SyncAppearancePacket(buf.readUUID(), BubbleAppearance.decode(buf));
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandler.handleSyncAppearance(playerId, appearance)));
        ctx.setPacketHandled(true);
    }
}
