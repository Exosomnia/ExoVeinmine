package com.exosomnia.exoveinmine.networking.packets;

import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VeinMinerChargePacket {

    private float charge = 0.0F;

    public VeinMinerChargePacket(float charge) {
        this.charge = charge;
    }

    public VeinMinerChargePacket(FriendlyByteBuf buffer) {
        charge = buffer.readFloat();
    }

    public static void encode(VeinMinerChargePacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.charge);
    }

    public static void handle(VeinMinerChargePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            NetworkDirection packetDirection = context.get().getDirection();
            if (packetDirection.equals(NetworkDirection.PLAY_TO_CLIENT)) {
                Minecraft.getInstance().player.getCapability(VeinMinerProvider.VEIN_MINER).ifPresent(veinData -> {
                    veinData.setCharge(packet.charge);
                    /*if (veinData.getCharge() >= 1000.0F) {
                        Minecraft.getInstance().player.playSound(RegistrationHandler.SOUND_VEIN_MINER_CHARGED.get(), 0.75f, 1.0f);
                    } //Gets annoying, so it's removed. */
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}
