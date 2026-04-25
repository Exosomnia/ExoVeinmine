package com.exosomnia.exoveinmine;

import com.exosomnia.exolib.ExoLib;
import com.exosomnia.exoveinmine.managers.VeinMinerManager;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// The value here should match an entry in the gradle.properties file
@Mod("exoveinmine")
public class ExoVeinMine
{
    public static final String MODID = "exoveinmine";
    public static final VeinMinerManager VEIN_MINER_MANAGER = new VeinMinerManager();

    public ExoVeinMine(IEventBus modEventBus, ModContainer modContainer) {
        Config config = new Config();
        config.loadInitConfig();
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(this::attributeModifyEvent);
        modEventBus.addListener(this::registerPackets);

        RegistrationHandler.register(modEventBus);

        ExoLib.CONFIG_SYNCHRONIZER.addConfig(config);

        modEventBus.addListener(config::onLoad);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(config::tagsUpdated);
    }

    public void attributeModifyEvent(final EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, RegistrationHandler.VEIN_MINER_CHARGE);
        event.add(EntityType.PLAYER, RegistrationHandler.VEIN_MINER_EFFICIENCY);
    }

    public void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ExoVeinMine.MODID);
        PacketHandler.register(registrar);
    }

    @SubscribeEvent
    public void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        if (!Config.enablePotions) return;

        PotionBrewing.Builder BUILDER = event.getBuilder();
        BUILDER.addMix(Potions.AWKWARD, Items.GLOW_BERRIES, RegistrationHandler.POTION_VEIN_MINER);
        BUILDER.addMix(RegistrationHandler.POTION_VEIN_MINER, Items.GLOWSTONE_DUST, RegistrationHandler.POTION_VEIN_MINER_STRONG);
        BUILDER.addMix(RegistrationHandler.POTION_VEIN_MINER, Items.REDSTONE, RegistrationHandler.POTION_VEIN_MINER_EXTENDED);
    }
}
