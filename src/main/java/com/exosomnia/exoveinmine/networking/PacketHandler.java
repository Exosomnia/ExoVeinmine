package com.exosomnia.exoveinmine.networking;

import com.exosomnia.exolib.networking.packets.ParticleShapePacket;
import com.exosomnia.exolib.networking.packets.SynchronizeConfigPacket;
import com.exosomnia.exolib.networking.packets.TagUpdatePacket;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerActivePacket;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerBreakPacket;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;


public class PacketHandler {

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(
                VeinMinerChargePacket.TYPE,
                VeinMinerChargePacket.STREAM_CODEC,
                VeinMinerChargePacket::handle
        );

        registrar.playToServer(
                VeinMinerActivePacket.TYPE,
                VeinMinerActivePacket.STREAM_CODEC,
                VeinMinerActivePacket::handle
        );

        registrar.playToClient(
                VeinMinerBreakPacket.TYPE,
                VeinMinerBreakPacket.STREAM_CODEC,
                VeinMinerBreakPacket::handle
        );
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }
}
