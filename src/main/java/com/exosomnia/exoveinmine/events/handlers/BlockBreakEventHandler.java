package com.exosomnia.exoveinmine.events.handlers;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.ExoVeinMine;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.controllers.VeinMinerController;
import com.exosomnia.exoveinmine.events.VeinMiningBreakEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExoVeinMine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockBreakEventHandler {

    @SubscribeEvent
    public static void breakEvent(BlockEvent.BreakEvent event){
        if (event.getLevel().isClientSide() || event.isCanceled() || event instanceof VeinMiningBreakEvent) return;

        ServerPlayer player = (ServerPlayer)event.getPlayer();
        UUID playerUUID = player.getUUID();
        if (!ExoVeinMine.VEIN_MINER_MANAGER.getPlayerActive(playerUUID)) return;

        if ( Config.globalEnable || player.getTags().contains(Config.tagName) ||
             (Config.enableEnchant && player.getMainHandItem().getEnchantmentLevel(RegistrationHandler.VEIN_MINER_ENCHANTMENT.get()) > 0)) {
            ExoVeinMine.VEIN_MINER_MANAGER.addController(playerUUID, new VeinMinerController(player,
                    event.getState().getBlock(), player.getMainHandItem(), player.serverLevel(), event.getPos()));
        }
    }
}
