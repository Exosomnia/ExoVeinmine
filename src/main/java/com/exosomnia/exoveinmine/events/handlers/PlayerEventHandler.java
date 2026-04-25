package com.exosomnia.exoveinmine.events.handlers;

import com.exosomnia.exoveinmine.ExoVeinMine;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ExoVeinMine.MODID)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event){
        ServerPlayer player = (ServerPlayer)event.getEntity();
        PacketHandler.sendToPlayer(player, new VeinMinerChargePacket(player.getData(RegistrationHandler.VEIN_MINER_DATA).floatValue()));
        ExoVeinMine.VEIN_MINER_MANAGER.addPlayerTracking(player.getUUID());
    }

    @SubscribeEvent
    public static void playerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event){
        ExoVeinMine.VEIN_MINER_MANAGER.removePlayerTracking(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void playerChangedDimensions(PlayerEvent.PlayerChangedDimensionEvent event){
        ServerPlayer player = (ServerPlayer)event.getEntity();
        PacketHandler.sendToPlayer(player, new VeinMinerChargePacket(player.getData(RegistrationHandler.VEIN_MINER_DATA).floatValue()));
    }
}
