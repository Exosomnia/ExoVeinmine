package com.exosomnia.exoveinmine;

import com.exosomnia.exolib.ExoLib;
import com.exosomnia.exolib.recipes.brewing.BrewingRecipeHelper;
import com.exosomnia.exoveinmine.capabilities.veinminer.IVeinMinerStorage;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import com.exosomnia.exoveinmine.managers.VeinMinerManager;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the gradle.properties file
@Mod("exoveinmine")
public class ExoVeinMine
{
    public static final String MODID = "exoveinmine";
    public static final VeinMinerManager VEIN_MINER_MANAGER = new VeinMinerManager();

    public ExoVeinMine() {
        Config config = new Config();
        config.loadInitConfig();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::attributeModifyEvent);

        PacketHandler.register();
        RegistrationHandler.register();

        ExoLib.CONFIG_SYNCHRONIZER.addConfig(config);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(config::onLoad);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(config::tagsUpdated);
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IVeinMinerStorage.class);
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "vein_miner_data"),  new VeinMinerProvider());
        }
    }

    public void attributeModifyEvent(final EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, RegistrationHandler.VEIN_MINER_CHARGE.get());
        event.add(EntityType.PLAYER, RegistrationHandler.VEIN_MINER_EFFICIENCY.get());
    }

    public void setupEvent(FMLCommonSetupEvent event) {
        if (!Config.enablePotions) return;

        event.enqueueWork(() -> {
            BrewingRecipeHelper.addSimplePotionRecipe(Potions.AWKWARD, Items.GLOW_BERRIES, RegistrationHandler.POTION_VEIN_MINER.get());
            BrewingRecipeHelper.addSimplePotionRecipe(RegistrationHandler.POTION_VEIN_MINER.get(), Items.GLOWSTONE_DUST, RegistrationHandler.POTION_VEIN_MINER_STRONG.get());
            BrewingRecipeHelper.addSimplePotionRecipe(RegistrationHandler.POTION_VEIN_MINER.get(), Items.REDSTONE, RegistrationHandler.POTION_VEIN_MINER_EXTENDED.get());
        });
    }
}
