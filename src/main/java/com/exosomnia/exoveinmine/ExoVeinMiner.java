package com.exosomnia.exoveinmine;

import com.exosomnia.exoveinmine.brewing.recipes.SimpleBrewingRecipe;
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
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

// The value here should match an entry in the gradle.properties file
@Mod("exoveinminer")
public class ExoVeinMiner
{
    public static final String MODID = "exoveinminer";

    public static final VeinMinerManager VEIN_MINER_MANAGER = new VeinMinerManager();

    public ExoVeinMiner() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        Config.loadInitConfig();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::attributeModifyEvent);

        MinecraftForge.EVENT_BUS.register(this);

        PacketHandler.register();
        RegistrationHandler.register();
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IVeinMinerStorage.class);
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(MODID, "vein_miner_data"), new VeinMinerProvider());
        }
    }

    public void attributeModifyEvent(final EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, RegistrationHandler.VEIN_MINER_CHARGE.get());
        event.add(EntityType.PLAYER, RegistrationHandler.VEIN_MINER_EFFICIENCY.get());
    }

    public void setupEvent(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BrewingRecipeRegistry.addRecipe(new SimpleBrewingRecipe(Potions.AWKWARD, Items.AMETHYST_SHARD, RegistrationHandler.POTION_VEIN_MINER.get()));
            //BrewingRecipeRegistry.addRecipe(new SimpleBrewingRecipe(RegistrationHandler.POTION_VEIN_MINER.get(), Items.GLOWSTONE_DUST, RegistrationHandler.POTION_VEIN_MINER_STRONG.get())); //Didn't feel like this fit in well.
            BrewingRecipeRegistry.addRecipe(new SimpleBrewingRecipe(RegistrationHandler.POTION_VEIN_MINER.get(), Items.REDSTONE, RegistrationHandler.POTION_VEIN_MINER_EXTENDED.get()));
        });
    }
}
