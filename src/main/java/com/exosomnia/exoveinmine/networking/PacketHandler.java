package com.exosomnia.exoveinmine.networking;

import com.exosomnia.exoveinmine.ExoVeinMiner;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerActivePacket;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExoVeinMiner.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        INSTANCE.registerMessage(id++, VeinMinerChargePacket.class, VeinMinerChargePacket::encode, VeinMinerChargePacket::new, VeinMinerChargePacket::handle);
        INSTANCE.registerMessage(id++, VeinMinerActivePacket.class, VeinMinerActivePacket::encode, VeinMinerActivePacket::new, VeinMinerActivePacket::handle);
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}
