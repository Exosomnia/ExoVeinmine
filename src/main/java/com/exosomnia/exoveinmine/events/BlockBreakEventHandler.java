package com.exosomnia.exoveinmine.events;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.ExoVeinMiner;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.controllers.VeinMinerController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExoVeinMiner.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockBreakEventHandler {

    @SubscribeEvent
    public static void breakEvent(BlockEvent.BreakEvent event){
        if (event.getLevel().isClientSide()) return;

        ServerPlayer player = (ServerPlayer)event.getPlayer();
        UUID playerUUID = player.getUUID();
        if (!ExoVeinMiner.VEIN_MINER_MANAGER.getPlayerActive(playerUUID)) return;

        if (    player.getTags().contains(Config.tagName) ||
                (Config.enableEnchant && player.getMainHandItem().getEnchantmentLevel(RegistrationHandler.VEIN_MINER_ENCHANTMENT.get()) > 0))  {

            ExoVeinMiner.VEIN_MINER_MANAGER.createController(playerUUID, new VeinMinerController(player,
                    event.getState().getBlock(), player.getMainHandItem(), player.serverLevel(), event.getPos()));
        }
    }
}
