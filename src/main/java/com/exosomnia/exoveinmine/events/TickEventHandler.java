package com.exosomnia.exoveinmine.events;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.ExoVeinMiner;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExoVeinMiner.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickEventHandler {

    static int playerTickCount = 0;

    @SubscribeEvent
    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
            ServerPlayer eventPlayer = (ServerPlayer)event.player;
            boolean actionsActive = ExoVeinMiner.VEIN_MINER_MANAGER.processControllers(eventPlayer.getUUID());
            if (playerTickCount++ % 20 == 0 && !actionsActive) {
                eventPlayer.getCapability(VeinMinerProvider.VEIN_MINER).ifPresent(data -> {
                    if (!data.isMax()) {
                        //Multiplies the default increment by our vein miner charge attribute and adds the product to our current charge, then send the updated charge value to client
                        data.setCharge(data.getCharge() + ((data.DEFAULT_INCREMENT * Config.chargeMod) * (float)eventPlayer.getAttributeValue(RegistrationHandler.VEIN_MINER_CHARGE.get())));
                        VeinMinerChargePacket packet = new VeinMinerChargePacket(data.getCharge());
                        PacketHandler.sendToPlayer(packet, eventPlayer);
                    }
                });
            }
        }
    }
}
