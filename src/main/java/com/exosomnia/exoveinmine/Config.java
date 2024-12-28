package com.exosomnia.exoveinmine;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = ExoVeinMiner.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue HIDE_BAR = BUILDER
            .comment("Hides the charge bar unless pressing the activation key")
            .define("hideBar", false);

    private static final ForgeConfigSpec.DoubleValue MAX_CHARGE = BUILDER
            .comment("Max charge value for vein miner's charge.")
            .defineInRange("maxCharge", 650.0, 0.0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue DEFAULT_INCREMENT = BUILDER
            .comment("Default value increments for vein mining.")
            .defineInRange("defaultIncrement", 25.0, 0.0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue CHARGE_MOD = BUILDER
            .comment("Mod value for charge rate.")
            .defineInRange("chargeMod", 0.2, 0.0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> TAG_NAME = BUILDER
            .comment("Entity tag string to allow for vein mining")
            .define("tagName", "exoveinmine");

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enableEnchant;
    public static boolean hideBar;
    public static float maxCharge;
    public static float defaultIncrement;
    public static float chargeMod;
    public static String tagName;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        hideBar = HIDE_BAR.get();
        maxCharge = MAX_CHARGE.get().floatValue();
        defaultIncrement = DEFAULT_INCREMENT.get().floatValue();
        chargeMod = CHARGE_MOD.get().floatValue();
        tagName = TAG_NAME.get();
    }

    static void loadInitConfig() {
        Path config = FMLPaths.CONFIGDIR.get();
        config = config.resolve("exoveinmine-init.txt");
        if (!Files.exists(config)) {
            try {
                Files.createFile(config);
                Files.writeString(config, "enableEnchant=true");
            }
            catch (IOException e) {
                LogUtils.getLogger().error("IOException when attempting to create exoveinmine-init.txt!");
            }
        }
        try {
            String enable = Files.readString(config);
            enableEnchant = Boolean.parseBoolean(enable.split("=")[1]);
        }
        catch (Exception e) {
            LogUtils.getLogger().error("Exception when attempting to read exoveinmine-init.txt! Defaulting to true.");
            enableEnchant = true;
        }
    }
}
