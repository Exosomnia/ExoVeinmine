package com.exosomnia.exoveinmine;

import com.exosomnia.exoveinmine.effects.VeinMinerEffect;
import com.exosomnia.exoveinmine.enchantments.EnchantmentVeinMiner;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ExoVeinMiner.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistrationHandler {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES,
            ExoVeinMiner.MODID);

    public static final RegistryObject<Attribute> VEIN_MINER_EFFICIENCY = ATTRIBUTES.register("vein_miner_efficiency",
            () -> new RangedAttribute("attribute.exoveinminer.vein_miner_efficiency",
                    1.0,
                    1.0,
                    2.0));
    public static final RegistryObject<Attribute> VEIN_MINER_CHARGE = ATTRIBUTES.register("vein_miner_charge",
            () -> new RangedAttribute("attribute.exoveinminer.vein_miner_charge",
                    1.0,
                    0.0,
                    1000.0));


    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,
            ExoVeinMiner.MODID);

    public static final RegistryObject<MobEffect> EFFECT_VEIN_MINER = MOB_EFFECTS.register("effect_vein_miner",
            () -> new VeinMinerEffect(MobEffectCategory.BENEFICIAL, 0x2ddde3) );


    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS,
            ExoVeinMiner.MODID);

    public static final RegistryObject<Potion> POTION_VEIN_MINER = POTIONS.register("potion_mining_proficiency",
            () -> new Potion(new MobEffectInstance(EFFECT_VEIN_MINER.get(), 3600, 0), new MobEffectInstance(MobEffects.DIG_SPEED, 3600, 0)));
    /*public static final RegistryObject<Potion> POTION_VEIN_MINER_STRONG = POTIONS.register("potion_vein_miner_strong",
            () -> new Potion(new MobEffectInstance(EFFECT_VEIN_MINER.get(), 3600, 1))); //Didn't feel like this fit in well. */
    public static final RegistryObject<Potion> POTION_VEIN_MINER_EXTENDED = POTIONS.register("potion_mining_proficiency_extended",
            () -> new Potion(new MobEffectInstance(EFFECT_VEIN_MINER.get(), 9600, 0), new MobEffectInstance(MobEffects.DIG_SPEED, 9600, 0)));


    //Removed all sounds from the mod as they were more annoying than anything.
    /*public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,
            ArchVeinMiner.MOD_ID);

    public static final RegistryObject<SoundEvent> SOUND_VEIN_MINER_CHARGED = SOUNDS.register("vein_miner_charged",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ArchVeinMiner.MOD_ID, "vein_miner_charged")));
    public static final RegistryObject<SoundEvent> SOUND_VEIN_MINER_ON = SOUNDS.register("vein_miner_toggle_on",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ArchVeinMiner.MOD_ID, "vein_miner_toggle_on")));
    public static final RegistryObject<SoundEvent> SOUND_VEIN_MINER_OFF = SOUNDS.register("vein_miner_toggle_off",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ArchVeinMiner.MOD_ID, "vein_miner_toggle_off")));*/


    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS,
            ExoVeinMiner.MODID);

    public static RegistryObject<Enchantment> VEIN_MINER_ENCHANTMENT = ENCHANTMENTS.register("vein_miner", EnchantmentVeinMiner::new);


    public static final KeyMapping ACTIVATE = new KeyMapping("key.exoveinminer.activate", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "key.categories.exoveinminer");


    public static void register() {
        ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
        POTIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MOB_EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        if (Config.enableEnchant) { ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus()); }
        //SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus()); //Removed all sounds from the mod as they were more annoying than anything.
    }

    @SubscribeEvent
    public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(ACTIVATE);
    }
}
