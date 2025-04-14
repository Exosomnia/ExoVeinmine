package com.exosomnia.exoveinmine.controllers;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.ExoVeinMiner;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import com.exosomnia.exoveinmine.capabilities.veinminer.IVeinMinerStorage;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerStorage;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerChargePacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private ServerLevel level;
    private Block block;
    private BlockPos[] blocksToSearch;
    private List<BlockPos> blocksNextSearch = new ArrayList<>();
    private Set<BlockPos> blocksSearched = new HashSet<>();
    private int iterationsLeft = 64;

    boolean enhanced;
    int fortuneLevel = 0;
    int silkTouchLevel = 0;

    public VeinMinerController(ServerPlayer player, Block block, ItemStack item, ServerLevel level, BlockPos position) {
        this.player = player;
        this.block = block;
        this.item = item;
        this.level = level;
        blocksToSearch = new BlockPos[]{position};
        blocksSearched.add(position);

        enhanced = player.getTags().contains(Config.tagNameEnhanced) || (item.getEnchantmentLevel(RegistrationHandler.VEIN_MINER_ENCHANTMENT.get()) > 1);
        if (!item.isEmpty()) {
            fortuneLevel = item.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            silkTouchLevel = item.getEnchantmentLevel(Enchantments.SILK_TOUCH);
        }
    }

    public ServerPlayer getPlayer() { return player; }

    //Returns true if the iteration is finished, false otherwise.
    public boolean iterate() {
        //Initial verifications, there are blocks to search and there are iterations left
        boolean finishedIterations = true;
        if (!(iterationsLeft-- > 0 && blocksToSearch.length != 0)) return finishedIterations;

        //Calculate charge penalty and verify we have enough charge before we begin
        IVeinMinerStorage data = player.getCapability(VeinMinerProvider.VEIN_MINER).resolve().orElse(new VeinMinerStorage(0.0F));
        float currentCharge = data.getCharge();
        //To calculate efficiency, X - (X * (Y)). X = Default increment, Y = Efficiency percentage (Defaults to 0%, hence the, Attribute - 1.0F)
        float chargePenalty = (data.DEFAULT_INCREMENT * (float)(2.0 - player.getAttributeValue(RegistrationHandler.VEIN_MINER_EFFICIENCY.get())));
        if (currentCharge < chargePenalty) return finishedIterations;

        //Used for the enhanced tag, will drop all items at the player instead of at the actual block
        ObjectArrayList<ItemStack> drops = new ObjectArrayList<>();
        int exp = 0;

        //Begin iterating through our blocksToSearch
        outer:
        {
            for (BlockPos position : blocksToSearch) {
                for (Vec3i offset : SEARCH_POSITIONS) {
                    BlockPos searchPos = position.offset(offset);
                    if (blocksSearched.contains(searchPos)) continue;

                    blocksSearched.add(searchPos);
                    BlockState searchState = level.getBlockState(searchPos);
                    if (!searchState.is(block)) continue;

                    //If the item stack that initiated this vein mine doesn't have enough durability left, stop the action.
                    if ((!player.gameMode.getGameModeForPlayer().equals(GameType.CREATIVE)) && (item.hurt(1, level.random, player))) {
                        item.setDamageValue(item.getMaxDamage() - 1);
                        break outer;
                    }

                    //We have made it past all checks, remove charge and break the block
                    currentCharge -= chargePenalty;
                    BlockEntity searchBlockEntity = searchState.hasBlockEntity() ? level.getBlockEntity(searchPos) : null;
                    player.awardStat(Stats.BLOCK_MINED.get(block));
                    player.causeFoodExhaustion(0.005F);
                    if (searchState.canHarvestBlock(level, searchPos, player)) {
                        if (enhanced) {
                            LootParams.Builder lootParams = (new LootParams.Builder(level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(searchPos)).withParameter(LootContextParams.TOOL, item).withOptionalParameter(LootContextParams.THIS_ENTITY, player).withOptionalParameter(LootContextParams.BLOCK_ENTITY, searchBlockEntity).withLuck(player.getLuck());
                            drops.addAll(searchState.getDrops(lootParams));
                            exp += searchState.getExpDrop(level, level.random, searchPos, fortuneLevel, silkTouchLevel);
                        } else {
                            Block.dropResources(searchState, level, searchPos, searchBlockEntity, player, item, true);
                        }
                    }
                    level.destroyBlock(searchPos, false);
                    if (blocksNextSearch.size() <= 64) { blocksNextSearch.add(searchPos); }

                    if (currentCharge < chargePenalty) break outer;
                }
            }
            blocksToSearch = blocksNextSearch.toArray(new BlockPos[0]);
            blocksNextSearch.clear();
            finishedIterations = false;
        }
        if (enhanced) {
            drops.forEach(drop -> Block.popResource(level, player.blockPosition(), drop));
            ExperienceOrb.award(level, player.position(), exp);
        }

        data.setCharge(currentCharge);
        PacketHandler.sendToPlayer(new VeinMinerChargePacket(currentCharge), player);
        return finishedIterations;
    }
}
