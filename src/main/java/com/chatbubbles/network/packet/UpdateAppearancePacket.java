package com.chatbubbles.network.packet;

import com.chatbubbles.data.BubbleAppearance;
import com.chatbubbles.data.PlayerBubbleData;
import com.chatbubbles.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class UpdateAppearancePacket {
    private final BubbleAppearance appearance;

    public UpdateAppearancePacket(BubbleAppearance appearance) {
        this.appearance = appearance;
    }

    public void encode(FriendlyByteBuf buf) {
        appearance.encode(buf);
    }

    public static UpdateAppearancePacket decode(FriendlyByteBuf buf) {
        return new UpdateAppearancePacket(BubbleAppearance.decode(buf));
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            PlayerBubbleData.set(player, appearance);
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SyncAppearancePacket(player.getUUID(), appearance)
            );
        });
        ctx.setPacketHandled(true);
    }
}
