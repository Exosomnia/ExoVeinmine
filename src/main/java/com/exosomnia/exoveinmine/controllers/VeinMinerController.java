package com.exosomnia.exoveinmine.controllers;

import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import com.exosomnia.exoveinmine.capabilities.veinminer.IVeinMinerStorage;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerStorage;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class VeinMinerController {

    //No, I will not automate this.
    private static final Vec3i[] SEARCH_POSITIONS = new Vec3i[]{
            //Search the 6 cardinal directions first
            new Vec3i(-1,0,0),
            new Vec3i(0,-1,0),
            new Vec3i(0,0,-1),
            new Vec3i(0,0,1),
            new Vec3i(0,1,0),
            new Vec3i(1,0,0),

            new Vec3i(-1,-1,-1),
            new Vec3i(-1,-1,0),
            new Vec3i(-1,-1,1),
            new Vec3i(-1,0,-1),
            new Vec3i(-1,0,1),
            new Vec3i(-1,1,-1),
            new Vec3i(-1,1,0),
            new Vec3i(-1,1,1),

            new Vec3i(0,-1,-1),
            new Vec3i(0,-1,1),
            new Vec3i(0,1,-1),
            new Vec3i(0,1,1),

            new Vec3i(1,-1,-1),
            new Vec3i(1,-1,0),
            new Vec3i(1,-1,1),
            new Vec3i(1,0,-1),
            new Vec3i(1,0,1),
            new Vec3i(1,1,-1),
            new Vec3i(1,1,0),
            new Vec3i(1,1,1)};

    private ServerPlayer player;
    private ItemStack item;
    private Level level;
    private Block block;
    private BlockPos[] blocksToSearch;
    private List<BlockPos> blocksNextSearch = new ArrayList<>();
    private List<BlockPos> blocksSearched = new ArrayList<>();
    private int iterationsLeft = 64;

    public VeinMinerController(ServerPlayer player, Block block, ItemStack item, Level level, BlockPos position) {
        this.player = player;
        this.block = block;
        this.item = item;
        this.level = level;
        blocksToSearch = new BlockPos[]{position};
        blocksSearched.add(position);
    }

    public ServerPlayer getPlayer() { return player; }

    //Returns true if the iteration is finished, false otherwise.
    public boolean iterate() {
        //Initial verifications, there are blocks to search and there are iterations left
        if (!(iterationsLeft-- > 0 && blocksToSearch.length != 0)) return true;

        //Calculate charge penalty and verify we have enough charge before we begin
        IVeinMinerStorage data = player.getCapability(VeinMinerProvider.VEIN_MINER).resolve().orElse(new VeinMinerStorage(0.0F));
        float currentCharge = data.getCharge();
        //To calculate efficiency, X - (X * (Y)). X = Default increment, Y = Efficiency percentage (Defaults to 0%, hence the, Attribute - 1.0F)
        float chargePenalty = (data.DEFAULT_INCREMENT * (1.0F / (float)player.getAttributeValue(RegistrationHandler.VEIN_MINER_EFFICIENCY.get())));
        if (currentCharge < chargePenalty) return true;

        //Begin iterating through our blocksToSearch
        outer:
        {
            for (BlockPos position : blocksToSearch) {
                for (Vec3i offset : SEARCH_POSITIONS) {
                    BlockPos searchPos = position.offset(offset);
                    if (blocksSearched.contains(searchPos)) continue;

                    blocksSearched.add(searchPos);
                    BlockState searchState = level.getBlockState(searchPos);
                    Block searchBlock = searchState.getBlock();
                    if (!searchBlock.equals(block)) continue;

                    //If the item stack that initiated this vein mine doesn't have enough durability left, stop the action.
                    if ((!player.gameMode.getGameModeForPlayer().equals(GameType.CREATIVE)) && (item.hurt(1, RandomSource.create(), player))) {
                        item.setDamageValue(item.getMaxDamage() - 1);
                        break outer;
                    }

                    //We have made it past all checks, remove charge and break the block
                    currentCharge -= chargePenalty;
                    BlockEntity searchBlockEntity = searchState.hasBlockEntity() ? level.getBlockEntity(searchPos) : null;
                    if (searchState.canHarvestBlock(level, searchPos,player)) { searchBlock.playerDestroy(level, player, searchPos, searchState, searchBlockEntity, item); }
                    level.destroyBlock(searchPos, false);
                    blocksNextSearch.add(searchPos);

                    if (currentCharge < chargePenalty) break outer;
                }
            }

            data.setCharge(currentCharge);
            PacketHandler.sendToPlayer(new VeinMinerChargePacket(currentCharge), player);
            blocksToSearch = blocksNextSearch.toArray(new BlockPos[0]);
            blocksNextSearch.clear();
            return false;
        }

        data.setCharge(currentCharge);
        PacketHandler.sendToPlayer(new VeinMinerChargePacket(currentCharge), player);
        return true;
    }
}
