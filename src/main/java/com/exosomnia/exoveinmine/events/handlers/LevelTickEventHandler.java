package com.exosomnia.exoveinmine.events.handlers;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.ExoVeinMine;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExoVeinMine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelTickEventHandler {

    @SubscribeEvent
    public static void levelTickEvent(TickEvent.LevelTickEvent event) {
        Level level = event.level;
        if (!event.phase.equals(TickEvent.Phase.START) || event.side != LogicalSide.SERVER) return;

        boolean chargeTick = level.getGameTime() % 20 == 0;
        for (Player player : level.players()) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            //If we have vein mining active, or it's not a recharge tick, skip the recharge
            if (ExoVeinMine.VEIN_MINER_MANAGER.processControllers(serverPlayer.getUUID()) || !chargeTick) continue;

            serverPlayer.getCapability(VeinMinerProvider.VEIN_MINER).ifPresent(data -> {
                if (data.isMax()) return;

                data.setCharge(data.getCharge() + ((Config.rechargeAmount) * serverPlayer.getAttributeValue(RegistrationHandler.VEIN_MINER_CHARGE.get())));
                PacketHandler.sendToPlayer(new VeinMinerChargePacket((float)data.getCharge()), serverPlayer);
            });
        }
    }
}
