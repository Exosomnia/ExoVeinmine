package com.exosomnia.exoveinmine.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;

public class VeinMiningBreakEvent extends BlockEvent.BreakEvent {

    public VeinMiningBreakEvent(Level level, BlockPos pos, BlockState state, Player player) {
        super(level, pos, state, player);
    }

}
