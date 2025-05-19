package com.exosomnia.exoveinmine.controllers;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.capabilities.veinminer.IVeinMinerStorage;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerStorage;
import com.exosomnia.exoveinmine.events.VeinMiningBreakEvent;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

public class VeinMinerController {

    private static final Vec3i[] SEARCH_POSITIONS = new Vec3i[]{
            //Search positions in order of distance (1 block distance > 2 > 3)
            new Vec3i(-1,0,0),
            new Vec3i(0,-1,0),
            new Vec3i(0,0,-1),
            new Vec3i(0,0,1),
            new Vec3i(0,1,0),
            new Vec3i(1,0,0),

            new Vec3i(-1,0,-1),
            new Vec3i(-1,0,1),
            new Vec3i(-1,-1,0),
            new Vec3i(-1,1,0),
            new Vec3i(1,0,-1),
            new Vec3i(1,0,1),
            new Vec3i(1,-1,0),
            new Vec3i(1,1,0),
            new Vec3i(0,-1,-1),
            new Vec3i(0,-1,1),
            new Vec3i(0,1,-1),
            new Vec3i(0,1,1),

            new Vec3i(-1,-1,-1),
            new Vec3i(-1,-1,1),
            new Vec3i(-1,1,-1),
            new Vec3i(-1,1,1),
            new Vec3i(1,-1,-1),
            new Vec3i(1,-1,1),
            new Vec3i(1,1,-1),
            new Vec3i(1,1,1)};

    private final ServerPlayer player;
    private final boolean noItem;
    private final ItemStack item;
    private final ServerLevel level;
    private final Block block;

    //Iteration and search related variables
    private final ArrayDeque<BlockPos> blocksToSearch = new ArrayDeque<>(27);
    private final List<BlockPos> blocksNextSearch = new ArrayList<>();
    private final Set<BlockPos> blocksSearched = new HashSet<>();
    private BlockPos remainingPos;
    private int iterationsLeft;
    private int maxBlocksPerIteration;

    //Variables that are set before the beginning of each iteration
    boolean isEnhanced;
    boolean isCreative;
    double currentCharge;
    double chargePenalty;
    int fortuneLevel = 0;
    int silkTouchLevel = 0;

    private record IterationResult(boolean shouldContinue, ObjectArrayList<ItemStack> drops, int exp){}

    public VeinMinerController(ServerPlayer player, Block block, ItemStack item, ServerLevel level, BlockPos position) {
        this.player = player;
        this.block = block;
        this.noItem = item.isEmpty();
        this.item = item;
        this.level = level;

        remainingPos = position;
        blocksSearched.add(position);

        iterationsLeft = Config.maxIterations;
        maxBlocksPerIteration = Config.maxBlocksPerIteration;
    }

    /***
     * Begins an iteration for this controller.
     * @return true if iteration should continue, false otherwise.
     */
    public boolean iterate() {
        //Initial verifications, and iteration setup
        IVeinMinerStorage data = player.getCapability(VeinMinerProvider.VEIN_MINER).resolve().orElse(new VeinMinerStorage(0.0));
        if (!setupIteration(data)) return false;

        IterationResult result = processIteration();
        blocksToSearch.addAll(blocksNextSearch);
        blocksNextSearch.clear();

        if (isEnhanced) {
            result.drops.forEach(drop -> Block.popResource(level, player.blockPosition(), drop));
            ExperienceOrb.award(level, player.position(), result.exp);
        }

        data.setCharge(currentCharge);
        PacketHandler.sendToPlayer(new VeinMinerChargePacket((float)currentCharge), player);
        return result.shouldContinue;
    }


    /***
     * Sets up the current iteration and validates the controller.
     * @param data IVeinMinerStorage of the player.
     * @return true if iteration should continue, false otherwise.
     */
    private boolean setupIteration(IVeinMinerStorage data) {
        //Begin validation checks and setting of charge variables
        if (!(iterationsLeft-- > 0 && (!blocksToSearch.isEmpty() || remainingPos != null) && !player.isRemoved())) return false;

        currentCharge = data.getCharge();
        chargePenalty = Math.min(Double.MAX_VALUE, data.CHARGE_PER_BLOCK * (1.0 / player.getAttributeValue(RegistrationHandler.VEIN_MINER_EFFICIENCY.get())));
        if (currentCharge < chargePenalty) return false;

        //If we started the action with an item, but now it's gone, return false.
        if (item.isEmpty()) {
            if (!noItem) {
                return false;
            }
        }
        //Checks complete, set remaining variables
        else {
            fortuneLevel = item.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            silkTouchLevel = item.getEnchantmentLevel(Enchantments.SILK_TOUCH);
        }

        isCreative = player.gameMode.getGameModeForPlayer().equals(GameType.CREATIVE);
        isEnhanced = Config.globalEnable || player.getTags().contains(Config.tagNameEnhanced) || (Config.enableEnchant && item.getEnchantmentLevel(RegistrationHandler.VEIN_MINER_ENCHANTMENT.get()) > 1);

        return true;
    }

    /***
     * Runs the mining logic for the iteration.
     * @return an IterationResult record of the iteration
     */
    private IterationResult processIteration() {
        //Used for the enhanced tag, will drop all items at the player instead of at the actual block
        ObjectArrayList<ItemStack> drops = new ObjectArrayList<>();
        int exp = 0;

        BlockPos originPos = remainingPos == null ? blocksToSearch.removeFirst() : remainingPos;
        remainingPos = null;
        int blocksLeft = maxBlocksPerIteration;
        while (originPos != null) {
            for (Vec3i offset : SEARCH_POSITIONS) {
                BlockPos searchPos = originPos.offset(offset);
                if (blocksSearched.contains(searchPos)) continue;

                blocksSearched.add(searchPos);
                BlockState searchState = level.getBlockState(searchPos);
                if (!searchState.is(block)) continue;

                //If the item stack that initiated this vein mine doesn't have enough durability left, stop the action.
                if ((!isCreative) && (item.hurt(1, level.random, player))) {
                    item.setDamageValue(item.getMaxDamage() - 1);
                    return new IterationResult(false, drops, exp);
                }

                VeinMiningBreakEvent event = new VeinMiningBreakEvent(level, searchPos, searchState, player);
                MinecraftForge.EVENT_BUS.post(event);
                if (event.isCanceled()) continue;

                //We have made it past all checks, remove charge, and break the block
                currentCharge -= chargePenalty;
                BlockEntity searchBlockEntity = searchState.hasBlockEntity() ? level.getBlockEntity(searchPos) : null;
                player.awardStat(Stats.BLOCK_MINED.get(block));
                player.causeFoodExhaustion(0.005F);
                if (searchState.canHarvestBlock(level, searchPos, player)) {
                    if (isEnhanced) {
                        LootParams.Builder lootParams = (new LootParams.Builder(level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(searchPos)).withParameter(LootContextParams.TOOL, item).withOptionalParameter(LootContextParams.THIS_ENTITY, player).withOptionalParameter(LootContextParams.BLOCK_ENTITY, searchBlockEntity).withLuck(player.getLuck());
                        drops.addAll(searchState.getDrops(lootParams));
                        exp += searchState.getExpDrop(level, level.random, searchPos, fortuneLevel, silkTouchLevel);
                    } else {
                        Block.dropResources(searchState, level, searchPos, searchBlockEntity, player, item, true);
                    }
                }
                level.destroyBlock(searchPos, false);
                blocksNextSearch.add(searchPos);

                if (currentCharge < chargePenalty) return new IterationResult(false, drops, exp);
                else if (--blocksLeft <= 0) {
                    remainingPos = originPos;
                    return new IterationResult(true, drops, exp);
                }
            }
            originPos = blocksToSearch.isEmpty() ? null : blocksToSearch.removeFirst();
        }
        return new IterationResult(!blocksNextSearch.isEmpty(), drops, exp);
    }
}
