package com.exosomnia.exoveinmine.networking.packets;

import com.exosomnia.exoveinmine.ExoVeinMine;
import com.exosomnia.exoveinmine.RegistrationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VeinMinerChargePacket(float charge) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VeinMinerChargePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "vein_mine_charge_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VeinMinerChargePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    VeinMinerChargePacket::charge,
                    VeinMinerChargePacket::new
            );

    @Override
    public CustomPacketPayload.Type<VeinMinerChargePacket> type() {
        return TYPE;
    }

    public static void handle(VeinMinerChargePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().player.setData(RegistrationHandler.VEIN_MINER_DATA, (double)packet.charge);
        });
    }
}
