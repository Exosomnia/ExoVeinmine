package com.exosomnia.exoveinmine.events.handlers;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.ExoVeinMine;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = ExoVeinMine.MODID)
public class LevelTickEventHandler {

    @SubscribeEvent
    public static void levelTickEvent(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        boolean chargeTick = level.getGameTime() % 20 == 0;
        for (Player player : level.players()) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            double charge = player.getData(RegistrationHandler.VEIN_MINER_DATA);

            //If we have vein mining active, it's not a recharge tick, or charge is max, skip the recharge
            if (ExoVeinMine.VEIN_MINER_MANAGER.processControllers(serverPlayer.getUUID()) || !chargeTick || charge >= Config.maxCharge) continue;

            double newCharge = Math.min(Config.maxCharge, charge + (Config.rechargeAmount * serverPlayer.getAttributeValue(RegistrationHandler.VEIN_MINER_CHARGE)));
            player.setData(RegistrationHandler.VEIN_MINER_DATA, newCharge);
            PacketHandler.sendToPlayer(serverPlayer, new VeinMinerChargePacket(player.getData(RegistrationHandler.VEIN_MINER_DATA).floatValue()));
        }
    }
}