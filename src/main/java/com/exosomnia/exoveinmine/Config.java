package com.exosomnia.exoveinmine;

import com.exosomnia.exolib.config.SynchronizableConfig;
import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config implements SynchronizableConfig {

    //region Config Builder
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue HIDE_BAR = BUILDER
            .comment("-[CLIENT SETTINGS]-")
            .comment("Toggles whether to hide the charge bar unless pressing the activation key.")
            .define("hideBar", true);

    private static final ForgeConfigSpec.BooleanValue SHOW_STATS = BUILDER
            .comment("Toggles whether to show stats of recharge rate and current charge when the charge bar is visible.")
            .define("showStats", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_ENCHANTMENT = BUILDER
            .comment("")
            .comment("-[ENCHANTMENT SETTINGS]-")
            .comment("Toggles whether the vein mining enchantment is enabled and obtainable.")
            .comment("NOTE: If globalEnable is toggled, the enchantment is automatically disabled unless")
            .comment("globalEnhancedRequiresEnchant is also toggled. This doesn't remove the enchantment")
            .comment("from tools, but does disable access to vein mining")
            .define("enableEnchant", true);

    private static final ForgeConfigSpec.BooleanValue TREASURE_ENCHANTMENT = BUILDER
            .comment("Toggles whether the vein mining enchantment is considered a treasure enchantment.")
            .define("treasureEnchant", true);

    private static final ForgeConfigSpec.BooleanValue TRADEABLE_ENCHANTMENT = BUILDER
            .comment("Toggles whether the vein mining enchantment can be traded for.")
            .define("tradeableEnchant", true);

    private static final ForgeConfigSpec.BooleanValue GLOBAL_ENHANCED_REQUIRES_ENCHANTMENT = BUILDER
            .comment("Toggles whether enhanced vein mining for globalEnable requires the vein mining enchantment.")
            .define("globalEnhancedRequiresEnchant", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_POTIONS = BUILDER
            .comment("")
            .comment("-[POTION SETTINGS]-")
            .comment("Toggles whether the vein mining enchantment is enabled and obtainable.")
            .comment("NOTE: This doesn't remove existing potions, just prevents them from being brewable.")
            .define("enablePotions", true);

    private static final ForgeConfigSpec.BooleanValue DISABLE_EFFECTS = BUILDER
            .comment("")
            .comment("-[EFFECT SETTINGS]-")
            .comment("Suppresses particle and sound effects from vein mining if true, can increase performance.")
            .comment("If disabled on the server, NO clients will receive effects.")
            .comment("If disabled on the client, only that client will ignore effects.")
            .define("disableEffects", false);

    private static final ForgeConfigSpec.BooleanValue GLOBAL_ENABLE = BUILDER
            .comment("")
            .comment("-[VEIN MINING SETTINGS]-")
            .comment("Allows vein mining (and the enhanced version) to be access even without the enchantment or tag.")
            .comment("If enableEnchant & globalEnhancedRequiresEnchant are both enabled with this option, enhanced.")
            .comment("vein mining will require the tool enchantment.")
            .define("globalEnable", false);

    private static final ForgeConfigSpec.IntValue MAX_ITERATIONS = BUILDER
            .comment("The maximum number of iterations for a vein mine action.")
            .defineInRange("maxIterations", 64, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MAX_BLOCKS_PER_ITERATION = BUILDER
            .comment("The maximum amount of blocks that can be broken in an iteration. Increase to speed up mining, decrease to slow.")
            .defineInRange("maxBlocksPerIteration", 5, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue ENABLE_CHARGE = BUILDER
            .comment("Determines if charge should be used when vein mining.")
            .define("enableCharge", true);

    private static final ForgeConfigSpec.DoubleValue MAX_CHARGE = BUILDER
            .comment("Max value for vein mine's charge.")
            .defineInRange("maxCharge", 192.0, 0.0, Double.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue CHARGE_PER_BLOCK = BUILDER
            .comment("Charge amount consumed per block when vein mining.")
            .defineInRange("chargePerBlock", 3.0, 0.0, Double.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue RECHARGE_AMOUNT = BUILDER
            .comment("Amount of charge regenerated a second.")
            .defineInRange("rechargeAmount", 1.0, 0.0, Double.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue DURABILITY_DAMAGE = BUILDER
            .comment("The amount of durability damage a tool takes from each block while vein mining.")
            .defineInRange("durabilityDamage", 1, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue EXHAUSTION_AMOUNT = BUILDER
            .comment("The amount of exhaustion accumulated for each block broken while vein mining.")
            .defineInRange("exhaustionAmount", 0.005, 0.0, 40.0);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_BLOCKS = BUILDER
            .comment("A list of blocks that cannot be vein mined, can be block ids, or tags.")
            .comment("Example: [\"minecraft:dirt\", \"#forge:stone\"]")
            .defineListAllowEmpty("blacklistedBlocks", List.of(), Config::validBlacklist);

    private static final ForgeConfigSpec.ConfigValue<String> TAG_NAME = BUILDER
            .comment("")
            .comment("-[TAG SETTINGS]-")
            .comment("Entity tag string to allow for vein mining.")
            .define("tagName", "exoveinmine");

    private static final ForgeConfigSpec.ConfigValue<String> TAG_NAME_ENHANCED = BUILDER
            .comment("Entity tag string to allow for enhanced vein mining.")
            .define("tagNameEnhanced", "exoveinmine-enhanced");

    static final ForgeConfigSpec SPEC = BUILDER.build();
    //endregion

    public static boolean enableCharge;
    public static boolean globalEnable;
    public static boolean globalEnhanced;
    public static boolean enableEnchant;
    public static boolean treasureEnchant;
    public static boolean tradeableEnchant;
    public static boolean globalEnhancedRequiresEnchant;
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

    public void onLoad(final ModConfigEvent event) {
        readFromFile();
    }

    public void tagsUpdated(final TagsUpdatedEvent event) {
        ResourceKey<Registry<Block>> blockRegistry = ForgeRegistries.BLOCKS.getRegistryKey();
        HashSet<Block> blacklistBuilder = new HashSet<>();

        event.getRegistryAccess().registry(blockRegistry).ifPresent(registry -> {
            for(String blacklistEntry : blacklistedStrings) {
                if (blacklistEntry.startsWith("#")) {
                    registry.getTagOrEmpty(TagKey.create(blockRegistry, ResourceLocation.bySeparator(blacklistEntry.substring(1), ':')))
                            .forEach(block -> blacklistBuilder.add(block.get()));
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
        buffer.writeBoolean(enableCharge);
        buffer.writeBoolean(enableEnchant);
        buffer.writeBoolean(globalEnable);
        buffer.writeDouble(maxCharge);
        buffer.writeDouble(chargePerBlock);
        buffer.writeDouble(rechargeAmount);
        buffer.writeUtf(tagName);
        buffer.writeUtf(tagNameEnhanced);
        return buffer;
    }

    @Override
    public SynchronizableConfig readFromBuffer(FriendlyByteBuf buffer) {
        enableCharge = buffer.readBoolean();
        enableEnchant = buffer.readBoolean();
        globalEnable = buffer.readBoolean();
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
        enableCharge = ENABLE_CHARGE.get();
        hideBar = HIDE_BAR.get();
        showStats = SHOW_STATS.get();
        enableEnchant = ENABLE_ENCHANTMENT.get();
        treasureEnchant = TREASURE_ENCHANTMENT.get();
        tradeableEnchant = TRADEABLE_ENCHANTMENT.get();
        globalEnhancedRequiresEnchant = GLOBAL_ENHANCED_REQUIRES_ENCHANTMENT.get();
        enablePotions = ENABLE_POTIONS.get();
        disableEffects = DISABLE_EFFECTS.get();
        globalEnable = GLOBAL_ENABLE.get();
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
        enableEnchant = Config.enableEnchant && !Config.globalEnable || (Config.enableEnchant && Config.globalEnhancedRequiresEnchant);
        globalEnhanced = Config.globalEnable && !Config.enableEnchant;
    }

    private static boolean validBlacklist(Object object) {
        return object instanceof String;
    }
}
