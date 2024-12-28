package com.exosomnia.exoveinmine.networking.packets;

import com.exosomnia.exoveinmine.ExoVeinMiner;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VeinMinerActivePacket {

    private boolean active = false;

    public VeinMinerActivePacket(boolean active) {
        this.active = active;
    }

    public VeinMinerActivePacket(FriendlyByteBuf buffer) {
        active = buffer.readBoolean();
    }

    public static void encode(VeinMinerActivePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
    }

    public static void handle(VeinMinerActivePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            NetworkDirection packetDirection = context.get().getDirection();
            if (packetDirection.equals(NetworkDirection.PLAY_TO_SERVER)) {
                ServerPlayer player = context.get().getSender();
                if (player != null) { ExoVeinMiner.VEIN_MINER_MANAGER.setPlayerActive(player.getUUID(), packet.active); }
            }
        });
        context.get().setPacketHandled(true);
    }
}
