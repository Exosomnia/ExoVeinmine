package com.exosomnia.exoveinmine.events;

import com.exosomnia.exoveinmine.ExoVeinMiner;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExoVeinMiner.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event){
        ServerPlayer player = (ServerPlayer)event.getEntity();
        player.getCapability(VeinMinerProvider.VEIN_MINER).ifPresent(data -> {
            VeinMinerChargePacket packet = new VeinMinerChargePacket(data.getCharge());
            PacketHandler.sendToPlayer(packet, player);
        });
        ExoVeinMiner.VEIN_MINER_MANAGER.addPlayer(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void playerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event){
        ExoVeinMiner.VEIN_MINER_MANAGER.removePlayer(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void playerChangedDimensions(PlayerEvent.PlayerChangedDimensionEvent event){
        ServerPlayer player = (ServerPlayer)event.getEntity();
        player.getCapability(VeinMinerProvider.VEIN_MINER).ifPresent(data -> {
            VeinMinerChargePacket packet = new VeinMinerChargePacket(data.getCharge());
            PacketHandler.sendToPlayer(packet, player);
        });
    }
}
