package com.exosomnia.exoveinmine;

import com.exosomnia.exolib.config.SynchronizableConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Mod.EventBusSubscriber(modid = ExoVeinMine.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config implements SynchronizableConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue HIDE_BAR = BUILDER
            .comment("Hides the charge bar unless pressing the activation key")
            .define("hideBar", true);

    private static final ForgeConfigSpec.IntValue MAX_ITERATIONS = BUILDER
            .comment("The maximum number of iterations for a vein mine action.")
            .defineInRange("maxIterations", 64, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MAX_BLOCKS_PER_ITERATION = BUILDER
            .comment("The maximum amount of blocks that can be broken in an iteration. Increase to speed up mining, decrease to slow.")
            .defineInRange("maxBlocksPerIteration", 6, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue MAX_CHARGE = BUILDER
            .comment("Max value for vein mine's charge.")
            .defineInRange("maxCharge", 200.0, 0.0, Double.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue CHARGE_PER_BLOCK = BUILDER
            .comment("Charge amount consumed per block when vein mining.")
            .defineInRange("chargePerBlock", 6.25, 0.0, Double.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue RECHARGE_AMOUNT = BUILDER
            .comment("Amount of charge regenerated a second.")
            .defineInRange("rechargeAmount", 1.5625, 0.0, Double.MAX_VALUE);

    private static final ForgeConfigSpec.ConfigValue<String> TAG_NAME = BUILDER
            .comment("Entity tag string to allow for vein mining")
            .define("tagName", "exoveinmine");

    private static final ForgeConfigSpec.ConfigValue<String> TAG_NAME_ENHANCED = BUILDER
            .comment("Entity tag string to allow for enhanced vein mining")
            .define("tagNameEnhanced", "exoveinmine-enhanced");

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean globalEnable = false;
    public static boolean enableEnchant = true;
    public static boolean treasureEnchant = true;
    public static boolean enablePotions = true;
    public static boolean hideBar;
    public static int maxIterations;
    public static int maxBlocksPerIteration;
    public static double maxCharge;
    public static double chargePerBlock;
    public static double rechargeAmount;
    public static String tagName;
    public static String tagNameEnhanced;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        hideBar = HIDE_BAR.get();
        maxIterations = MAX_ITERATIONS.get();
        maxBlocksPerIteration = MAX_BLOCKS_PER_ITERATION.get();
        maxCharge = MAX_CHARGE.get();
        chargePerBlock = CHARGE_PER_BLOCK.get();
        rechargeAmount = RECHARGE_AMOUNT.get();
        tagName = TAG_NAME.get();
        tagNameEnhanced = TAG_NAME_ENHANCED.get();
    }

    static void loadInitConfig() {
        Path config = FMLPaths.CONFIGDIR.get();
        config = config.resolve("exoveinmine-init.txt");
        if (!Files.exists(config)) {
            try {
                Files.copy(Config.class.getResourceAsStream("/data/exoveinmine/config/exoveinmine-init.txt"), config, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e) {
                LogUtils.getLogger().error("IOException when attempting to copy exoveinmine-init.txt!");
            }
        }
        try {
            List<String> initLines = Files.readAllLines(config);
            for (String line : initLines) {
                line = line.trim();
                if (line.startsWith("#")) continue;

                String[] configLine = line.split("=");
                if (configLine.length < 2) continue;

                String configValue = configLine[1].trim();
                switch (configLine[0].trim()) {
                    case "globalEnabled":
                        globalEnable = Boolean.parseBoolean(configValue);
                        break;
                    case "enableEnchant":
                        enableEnchant = Boolean.parseBoolean(configValue);
                        break;
                    case "treasureEnchant":
                        treasureEnchant = Boolean.parseBoolean(configValue);
                        break;
                    case "enablePotions":
                        enablePotions = Boolean.parseBoolean(configValue);
                        break;
                }
            }
        }
        catch (Exception e) {
            LogUtils.getLogger().error("Exception when attempting to read exoveinmine-init.txt!");
        }
    }

    @Override
    public FriendlyByteBuf writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBoolean(globalEnable);
        buffer.writeDouble(maxCharge);
        buffer.writeUtf(tagName);
        buffer.writeUtf(tagNameEnhanced);
        return buffer;
    }

    @Override
    public SynchronizableConfig readFromBuffer(FriendlyByteBuf buffer) {
        globalEnable = buffer.readBoolean();
        maxCharge = buffer.readDouble();
        tagName = buffer.readUtf();
        tagNameEnhanced = buffer.readUtf();
        return this;
    }

    @Override
    public void readFromFile() {
        Config.loadInitConfig();

        hideBar = HIDE_BAR.get();
        maxIterations = MAX_ITERATIONS.get();
        maxBlocksPerIteration = MAX_BLOCKS_PER_ITERATION.get();
        maxCharge = MAX_CHARGE.get();
        chargePerBlock = CHARGE_PER_BLOCK.get();
        rechargeAmount = RECHARGE_AMOUNT.get();
        tagName = TAG_NAME.get();
        tagNameEnhanced = TAG_NAME_ENHANCED.get();
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "config");
    }
}
