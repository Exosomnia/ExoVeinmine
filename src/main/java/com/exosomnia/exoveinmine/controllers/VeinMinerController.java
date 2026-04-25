package com.exosomnia.exoveinmine.controllers;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.events.VeinMiningBreakEvent;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerBreakPacket;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import com.exosomnia.exoveinmine.util.VeinMineHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

public class VeinMinerController {

    private static final double VISUAL_EFFECT_RANGE = 32.0 * 32.0;
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
    private final Vec3 initialPos;

    //Iteration and search related variables
    private final ArrayDeque<BlockPos> blocksToSearch = new ArrayDeque<>(27);
    private final List<BlockPos> blocksNextSearch = new ArrayList<>();
    private final Set<BlockPos> blocksSearched = new HashSet<>();
    private BlockPos remainingPos;
    private int iterationsLeft;
    private int maxBlocksPerIteration;

    private final ObjectArrayList<ItemStack> drops = new ObjectArrayList<>();
    private final List<BlockPos> brokenThisOrigin = new ArrayList<>();

    //Variables that are set before the beginning of each iteration
    boolean isEnhanced;
    boolean isCreative;
    double currentCharge;
    double chargePenalty;
    int fortuneLevel = 0;
    int silkTouchLevel = 0;
    LootParams.Builder lootParams;

    private record IterationResult(boolean shouldContinue, int exp){}

    public VeinMinerController(ServerPlayer player, Block block, ItemStack item, ServerLevel level, BlockPos position) {
        this.player = player;
        this.block = block;
        this.noItem = item.isEmpty();
        this.item = item;
        this.level = level;
        this.initialPos = position.getCenter();

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
        if (!setupIteration(player.getData(RegistrationHandler.VEIN_MINER_DATA))) return false;

        IterationResult result = processIteration();
        blocksToSearch.addAll(blocksNextSearch);
        blocksNextSearch.clear();

        if (isEnhanced && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !level.restoringBlockSnapshots) {
            drops.forEach(drop -> {
                if (!drop.isEmpty()) {
                    ItemEntity dropEntity = new ItemEntity(level, initialPos.x, initialPos.y, initialPos.z, drop);
                    dropEntity.setPickUpDelay(5);
                    level.addFreshEntity(dropEntity);
                }
            });
            ExperienceOrb.award(level, player.position(), result.exp);
        }

        if (Config.enableCharge) {
            player.setData(RegistrationHandler.VEIN_MINER_DATA, currentCharge);
            PacketHandler.sendToPlayer(player, new VeinMinerChargePacket((float) currentCharge));
        }
        return result.shouldContinue;
    }


    /***
     * Sets up the current iteration and validates the controller.
     * @param charge charge of the player.
     * @return true if iteration should continue, false otherwise.
     */
    private boolean setupIteration(double charge) {
        Holder<Enchantment> veinMineEnchant = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(RegistrationHandler.VEIN_MINER_ENCHANTMENT);

        //Begin validation checks and setting of charge variables
        if (!(iterationsLeft-- > 0 && (!blocksToSearch.isEmpty() || remainingPos != null) && !player.isRemoved())) return false;

        currentCharge = charge;
        chargePenalty = 0.0;
        if (Config.enableCharge) {
            chargePenalty = Math.min(Double.MAX_VALUE, Config.chargePerBlock * (1.0 / player.getAttributeValue(RegistrationHandler.VEIN_MINER_EFFICIENCY)));
            if (currentCharge < chargePenalty) return false;
        }

        //If we started the action with an item, but now it's gone, return false.
        if (item.isEmpty()) {
            if (!noItem) {
                return false;
            }
        }

        //Checks complete, set remaining variables
        else {
            fortuneLevel = item.getEnchantmentLevel(level.registryAccess().lookup(Registries.ENCHANTMENT).get().getOrThrow(Enchantments.FORTUNE));
            silkTouchLevel = item.getEnchantmentLevel(level.registryAccess().lookup(Registries.ENCHANTMENT).get().getOrThrow(Enchantments.SILK_TOUCH));
        }

        isCreative = player.gameMode.getGameModeForPlayer().equals(GameType.CREATIVE);
        isEnhanced = VeinMineHelper.checkEnhanced(player, level);
        lootParams = new LootParams.Builder(level).withParameter(LootContextParams.TOOL, item).withOptionalParameter(LootContextParams.THIS_ENTITY, player).withLuck(player.getLuck());

        return true;
    }

    /***
     * Runs the mining logic for the iteration.
     * @return an IterationResult record of the iteration
     */
    private IterationResult processIteration() {
        //Used for the enhanced tag, will drop all items at the player instead of at the actual block
        drops.clear();
        int exp = 0;

        //If we have a remaining position from last iteration, resume iterating from that position.
        BlockPos originPos = remainingPos == null ? blocksToSearch.removeFirst() : remainingPos;
        remainingPos = null;

        int blocksLeft = maxBlocksPerIteration;
        while (originPos != null) {
            brokenThisOrigin.clear();

            for (Vec3i offset : SEARCH_POSITIONS) {
                BlockPos searchPos = originPos.offset(offset);
                if (blocksSearched.contains(searchPos)) continue;

                blocksSearched.add(searchPos);
                BlockState searchState = level.getBlockState(searchPos);
                if (!searchState.is(block)) continue;

                //If the item stack that initiated this vein mine doesn't have enough durability left, stop the action.
                if (!isCreative && item.isDamageableItem()) {
                    int damage = EnchantmentHelper.processDurabilityChange(level, item, Config.durabilityDamage);
                    if (item.getMaxDamage() - item.getDamageValue() <= damage) {
                        item.setDamageValue(item.getMaxDamage() - 1);
                        return new IterationResult(false, exp);
                    }
                    item.setDamageValue(item.getDamageValue() + damage);
                }

                VeinMiningBreakEvent event = new VeinMiningBreakEvent(level, searchPos, searchState, player);
                NeoForge.EVENT_BUS.post(event);
                if (event.isCanceled()) continue;

                //We have made it past all checks, remove charge, and break the block
                currentCharge -= chargePenalty;
                BlockEntity searchBlockEntity = searchState.hasBlockEntity() ? level.getBlockEntity(searchPos) : null;
                player.awardStat(Stats.BLOCK_MINED.get(block));
                if (!(Config.enchantNegatesExhaustion && VeinMineHelper.hasEnchantment(item, level))) player.causeFoodExhaustion(Config.exhaustionAmount);
                if (!searchState.requiresCorrectToolForDrops() || item.isCorrectToolForDrops(searchState)) {
                    if (isEnhanced) {
                        drops.addAll(searchState.getDrops(lootParams.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(searchPos)).withOptionalParameter(LootContextParams.BLOCK_ENTITY, searchBlockEntity)));
                        exp += searchState.getExpDrop(level, searchPos, searchBlockEntity, player, item);
                    } else {
                        Block.dropResources(searchState, level, searchPos, searchBlockEntity, player, item);
                    }
                }
                if (level.removeBlock(searchPos, false)) {
                    level.gameEvent(GameEvent.BLOCK_DESTROY, searchPos, GameEvent.Context.of(player, searchState));
                }
                brokenThisOrigin.add(searchPos);

                if (currentCharge < chargePenalty) return new IterationResult(false, exp);
                else if (--blocksLeft <= 0) {
                    //Handle Effects
                    if (!Config.disableEffects) sendVisualEffects(originPos, brokenThisOrigin);

                    blocksNextSearch.addAll(brokenThisOrigin);
                    remainingPos = originPos;
                    return new IterationResult(true, exp);
                }
            }
            //Handle Effects
            if (!Config.disableEffects) sendVisualEffects(originPos, brokenThisOrigin);

            blocksNextSearch.addAll(brokenThisOrigin);
            originPos = blocksToSearch.isEmpty() ? null : blocksToSearch.removeFirst();
        }
        return new IterationResult(!blocksNextSearch.isEmpty(), exp);
    }

    private void sendVisualEffects(BlockPos origin, List<BlockPos> positions) {
        VeinMinerBreakPacket packet = new VeinMinerBreakPacket(Block.getId(block.defaultBlockState()), origin, positions);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(origin.getCenter()) < VISUAL_EFFECT_RANGE) PacketHandler.sendToPlayer(player, packet);
        }
    }
}
