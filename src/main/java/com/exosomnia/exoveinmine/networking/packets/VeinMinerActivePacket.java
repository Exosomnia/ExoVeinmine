package com.exosomnia.exoveinmine.networking.packets;

import com.exosomnia.exoveinmine.ExoVeinMine;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VeinMinerActivePacket(boolean active) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VeinMinerActivePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "vein_mine_active_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VeinMinerActivePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    VeinMinerActivePacket::active,
                    VeinMinerActivePacket::new
            );

    @Override
    public CustomPacketPayload.Type<VeinMinerActivePacket> type() {
        return TYPE;
    }

    public static void handle(VeinMinerActivePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ExoVeinMine.VEIN_MINER_MANAGER.setPlayerActive(player.getUUID(), packet.active);
        });
    }
}
