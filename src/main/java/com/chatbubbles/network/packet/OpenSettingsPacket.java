package com.chatbubbles.network.packet;

import com.chatbubbles.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenSettingsPacket {
    public OpenSettingsPacket() {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static OpenSettingsPacket decode(FriendlyByteBuf buf) {
        return new OpenSettingsPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHandler::handleOpenSettings));
        ctx.setPacketHandled(true);
    }
}
