package com.exosomnia.exoveinmine.events.handlers;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.ExoVeinMine;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.controllers.VeinMinerController;
import com.exosomnia.exoveinmine.events.VeinMiningBreakEvent;
import com.exosomnia.exoveinmine.util.VeinMineHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.UUID;

@EventBusSubscriber(modid = ExoVeinMine.MODID)
public class BlockBreakEventHandler {

    @SubscribeEvent
    public static void breakEvent(BlockEvent.BreakEvent event){
        Block eventBlock = event.getState().getBlock();
        LevelAccessor level = event.getLevel();
        if (level.isClientSide() || event.isCanceled() || event instanceof VeinMiningBreakEvent || Config.blacklist.contains(eventBlock)) return;

        ServerPlayer player = (ServerPlayer)event.getPlayer();
        UUID playerUUID = player.getUUID();
        if (!ExoVeinMine.VEIN_MINER_MANAGER.getPlayerActive(playerUUID)) return;

        var requirementCheck = VeinMineHelper.checkRequirements(player, level);
        if (requirementCheck != null) {
            player.displayClientMessage(Component.translatable(requirementCheck.error()).withStyle(ChatFormatting.RED), true);
            return;
        }

        if (VeinMineHelper.checkAccess(player, level)) {
            ExoVeinMine.VEIN_MINER_MANAGER.addController(playerUUID, new VeinMinerController(player,
                    eventBlock, player.getMainHandItem(), player.serverLevel(), event.getPos()));
        }
    }
}
