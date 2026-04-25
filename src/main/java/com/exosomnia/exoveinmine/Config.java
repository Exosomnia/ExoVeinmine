package com.exosomnia.exoveinmine;

import com.exosomnia.exolib.config.SynchronizableConfig;
import com.exosomnia.exoveinmine.util.VeinMineHelper;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Config implements SynchronizableConfig {

    //region Config Builder
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue HIDE_BAR = BUILDER
            .comment("-[CLIENT SETTINGS]-")
            .comment("(True): Only displays the charge bar when the vein mine key is pressed.")
            .comment("(False): Displays the charge bar when the vein mine key is pressed, or if charge is not full.")
            .define("hideBar", true);

    private static final ModConfigSpec.BooleanValue SHOW_STATS = BUILDER
            .comment("Toggles whether to show stats of current charge and recharge rate when the charge bar is visible.")
            .define("showStats", true);

    private static final ModConfigSpec.BooleanValue ENHANCED_REQUIRES_ENCHANTMENT = BUILDER
            .comment("")
            .comment("-[ENCHANTMENT SETTINGS]-")
            .comment("As of 1.21, enchantments are now defined via datapacks and json files.")
            .comment("To edit enchantment settings, you will need to make a datapack with the")
            .comment("configurations you want for the enchantment.")
            .comment("")
            .comment("By default, the enchantment is enabled, is considered a non-treasure enchant")
            .comment("and can be obtained by trading with villagers or at the enchanting table.")
            .comment("")
            .comment("If the setting 'globalEnable' is enabled, this setting requires an item with")
            .comment("the enchantment to access enhanced vein mining.")
            .define("enhancedRequiresEnchant", false);

    private static final ModConfigSpec.BooleanValue ENCHANTMENT_NEGATES_EXHAUSTION = BUILDER
            .comment("If true, vein mining with a tool enchanted with vein miner negates hunger cost.")
            .define("enchantNegatesExhaustion", false);

    private static final ModConfigSpec.BooleanValue ENABLE_POTIONS = BUILDER
            .comment("")
            .comment("-[POTION SETTINGS]-")
            .comment("Toggles whether the vein mining potions are enabled and obtainable.")
            .comment("NOTE: This doesn't remove the potions, just prevents them from being brewable.")
            .define("enablePotions", true);

    private static final ModConfigSpec.BooleanValue DISABLE_EFFECTS = BUILDER
            .comment("")
            .comment("-[EFFECT SETTINGS]-")
            .comment("Suppresses particle and sound effects from vein mining if true, can increase performance.")
            .comment("If disabled on the server, NO clients will receive effects.")
            .comment("If disabled on the client, only that client will ignore effects.")
            .define("disableEffects", false);

    private static final ModConfigSpec.BooleanValue GLOBAL_ENABLE = BUILDER
            .comment("")
            .comment("-[VEIN MINING SETTINGS]-")
            .comment("Allows vein mining (and the enhanced version) to be accessed even without the enchantment or tag.")
            .comment("If 'enhancedRequiresEnchant' is also enabled, enhanced vein mining will require the enchantment.")
            .define("globalEnable", true);

    private static final ModConfigSpec.BooleanValue TOOL_REQUIRED = BUILDER
            .comment("(True): Vein mining requires a tool like a pickaxe, axe, etc...")
            .comment("(False): Vein mining can be accessed with any item, or empty hand.")
            .define("miningToolRequired", false);

    private static final ModConfigSpec.IntValue MAX_ITERATIONS = BUILDER
            .comment("The maximum number of iterations for a vein mine action.")
            .defineInRange("maxIterations", 64, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MAX_BLOCKS_PER_ITERATION = BUILDER
            .comment("The maximum amount of blocks that can be broken in an iteration. Increase to speed up mining, decrease to slow.")
            .defineInRange("maxBlocksPerIteration", 5, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue ENABLE_CHARGE = BUILDER
            .comment("Determines if charge should be used when vein mining.")
            .define("enableCharge", true);

    private static final ModConfigSpec.DoubleValue MAX_CHARGE = BUILDER
            .comment("Max value for vein mine's charge.")
            .defineInRange("maxCharge", 192.0, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue CHARGE_PER_BLOCK = BUILDER
            .comment("Charge amount consumed per block when vein mining.")
            .defineInRange("chargePerBlock", 3.0, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue RECHARGE_AMOUNT = BUILDER
            .comment("Amount of charge regenerated a second.")
            .defineInRange("rechargeAmount", 1.0, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.IntValue DURABILITY_DAMAGE = BUILDER
            .comment("The amount of durability damage a tool takes from each block while vein mining.")
            .defineInRange("durabilityDamage", 1, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue EXHAUSTION_AMOUNT = BUILDER
            .comment("The amount of exhaustion accumulated for each block broken while vein mining.")
            .comment("NOTE: In vanilla, breaking a block costs 0.005 exhaustion.")
            .defineInRange("exhaustionAmount", 0.0075, 0.0, 40.0);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_BLOCKS = BUILDER
            .comment("A list of blocks that cannot be vein mined, can be block ids, or tags.")
            .comment("Example: [\"minecraft:dirt\", \"#c:stone\"]")
            .defineListAllowEmpty("blacklistedBlocks", List.of(), Config::validBlacklist);

    private static final ModConfigSpec.ConfigValue<String> TAG_NAME = BUILDER
            .comment("")
            .comment("-[TAG SETTINGS]-")
            .comment("Entity tag string to allow for vein mining.")
            .define("tagName", "exoveinmine");

    private static final ModConfigSpec.ConfigValue<String> TAG_NAME_ENHANCED = BUILDER
            .comment("Entity tag string to allow for enhanced vein mining.")
            .define("tagNameEnhanced", "exoveinmine-enhanced");

    static final ModConfigSpec SPEC = BUILDER.build();
    //endregion

    public static boolean globalEnable;
    public static boolean toolRequired;
    public static boolean enableCharge;
    public static boolean globalEnhanced;
    public static boolean enhancedRequiresEnchant;
    public static boolean enchantNegatesExhaustion;
    public static boolean enablePotions;
    public static boolean hideBar;
    public static boolean showStats;
    public static boolean disableEffects;
    public static int maxIterations;
    public static int maxBlocksPerIteration;
    public static double maxCharge;
    public static double chargePerBlock;
    public static double rechargeAmount;
    public static int durabilityDamage;
    public static float exhaustionAmount;
    public static String tagName;
    public static String tagNameEnhanced;

    private static Set<String> blacklistedStrings;
    public static ImmutableSet<Block> blacklist = ImmutableSet.of();

    public void onLoad(final ModConfigEvent event) { readFromFile(); }

    public void tagsUpdated(final TagsUpdatedEvent event) {
        ResourceKey<Registry<Block>> blockRegistry = Registries.BLOCK;
        HashSet<Block> blacklistBuilder = new HashSet<>();

        event.getRegistryAccess().registry(blockRegistry).ifPresent(registry -> {
            for(String blacklistEntry : blacklistedStrings) {
                if (blacklistEntry.startsWith("#")) {
                    registry.getTagOrEmpty(TagKey.create(blockRegistry, ResourceLocation.bySeparator(blacklistEntry.substring(1), ':')))
                            .forEach(block -> blacklistBuilder.add(block.value()));
                }
                else {
                    Block block = registry.get(ResourceLocation.bySeparator(blacklistEntry, ':'));
                    if (block != null) blacklistBuilder.add(block);
                }
            }
        });

        blacklist = ImmutableSet.copyOf(blacklistBuilder);
    }

    //Removed as all init config settings were moved to regular config, keeping this, just commented out for reference since I don't believe this code was pushed to the repo yet? Will remove after next push
    public void loadInitConfig() {
//        Path config = FMLPaths.CONFIGDIR.get();
//        config = config.resolve("exoveinmine-init.txt");
//        if (!Files.exists(config)) {
//            try {
//                Files.copy(Config.class.getResourceAsStream("/data/exoveinmine/config/exoveinmine-init.txt"), config, StandardCopyOption.REPLACE_EXISTING);
//            }
//            catch (IOException e) {
//                LogUtils.getLogger().error("IOException when attempting to copy exoveinmine-init.txt!");
//            }
//        }
//        try {
//            List<String> initLines = Files.readAllLines(config);
//            for (String line : initLines) {
//                line = line.trim();
//                if (line.startsWith("#")) continue;
//
//                String[] configLine = line.split("=");
//                if (configLine.length < 2) continue;
//
//                String configValue = configLine[1].trim();
//                switch (configLine[0].trim()) {
//                    case "globalEnabled":
//                        globalEnable = Boolean.parseBoolean(configValue);
//                        break;
//                }
//            }
//        }
//        catch (Exception e) {
//            LogUtils.getLogger().error("Exception when attempting to read exoveinmine-init.txt!");
//        }
    }

    @Override
    public FriendlyByteBuf writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBoolean(globalEnable);
        buffer.writeBoolean(toolRequired);
        buffer.writeBoolean(enableCharge);
        buffer.writeDouble(maxCharge);
        buffer.writeDouble(chargePerBlock);
        buffer.writeDouble(rechargeAmount);
        buffer.writeUtf(tagName);
        buffer.writeUtf(tagNameEnhanced);
        return buffer;
    }

    @Override
    public SynchronizableConfig readFromBuffer(FriendlyByteBuf buffer) {
        globalEnable = buffer.readBoolean();
        toolRequired = buffer.readBoolean();
        enableCharge = buffer.readBoolean();
        maxCharge = buffer.readDouble();
        chargePerBlock = buffer.readDouble();
        rechargeAmount = buffer.readDouble();
        tagName = buffer.readUtf();
        tagNameEnhanced = buffer.readUtf();

        processConfig();
        return this;
    }

    @Override
    public void readFromFile() {
        globalEnable = GLOBAL_ENABLE.get();
        toolRequired = TOOL_REQUIRED.get();
        enableCharge = ENABLE_CHARGE.get();
        hideBar = HIDE_BAR.get();
        showStats = SHOW_STATS.get();
        enhancedRequiresEnchant = ENHANCED_REQUIRES_ENCHANTMENT.get();
        enchantNegatesExhaustion = ENCHANTMENT_NEGATES_EXHAUSTION.get();
        enablePotions = ENABLE_POTIONS.get();
        disableEffects = DISABLE_EFFECTS.get();
        maxIterations = MAX_ITERATIONS.get();
        maxBlocksPerIteration = MAX_BLOCKS_PER_ITERATION.get();
        maxCharge = MAX_CHARGE.get();
        chargePerBlock = CHARGE_PER_BLOCK.get();
        rechargeAmount = RECHARGE_AMOUNT.get();
        durabilityDamage = DURABILITY_DAMAGE.get();
        exhaustionAmount = EXHAUSTION_AMOUNT.get().floatValue();
        tagName = TAG_NAME.get();
        tagNameEnhanced = TAG_NAME_ENHANCED.get();

        blacklistedStrings = ImmutableSet.copyOf(BLACKLISTED_BLOCKS.get());
        processConfig();
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "config");
    }

    private void processConfig() {
        globalEnhanced = Config.globalEnable && !Config.enhancedRequiresEnchant;

        Set<VeinMineHelper.Requirement> accessRequirements = new HashSet<>();
        Set<BiPredicate<Player, LevelAccessor>> accessChecks = new HashSet<>();
        Set<BiPredicate<Player, LevelAccessor>> enhancedChecks = new HashSet<>();

        //If global is not enabled, check for enchantment or tag
        if (!globalEnable) {
            accessChecks.add(VeinMineHelper.HAS_ENCHANTMENT);
            accessChecks.add(VeinMineHelper.HAS_ACCESS_TAG);
        }

        if (toolRequired) {
            accessRequirements.add(VeinMineHelper.TOOL_REQUIRED_PREDICATE);
        }

        //If global is not enhanced, check for enchantment, or tag
        if (!globalEnhanced) {
            enhancedChecks.add(VeinMineHelper.HAS_ENCHANTMENT);
            enhancedChecks.add(VeinMineHelper.HAS_ENHANCED_TAG);
        }

        VeinMineHelper.setPredicates(accessRequirements, accessChecks, enhancedChecks);
    }

    private static boolean validBlacklist(Object object) {
        return object instanceof String;
    }
}
