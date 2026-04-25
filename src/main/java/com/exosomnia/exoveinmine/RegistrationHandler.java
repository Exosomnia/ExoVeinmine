package com.exosomnia.exoveinmine;

import com.exosomnia.exoveinmine.effects.VeinMinerEffect;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;


public class RegistrationHandler {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE,
            ExoVeinMine.MODID);

    public static final DeferredHolder<Attribute, Attribute> VEIN_MINER_EFFICIENCY = ATTRIBUTES.register("vein_miner_efficiency",
            () -> new RangedAttribute("attribute.exoveinmine.vein_miner_efficiency",
                    1.0,
                    Double.MIN_VALUE,
                    1000.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> VEIN_MINER_CHARGE = ATTRIBUTES.register("vein_miner_charge",
            () -> new RangedAttribute("attribute.exoveinmine.vein_miner_charge",
                    1.0,
                    0.0,
                    1000.0).setSyncable(true));


    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT,
            ExoVeinMine.MODID);

    public static final DeferredHolder<MobEffect, VeinMinerEffect> EFFECT_VEIN_MINER = MOB_EFFECTS.register("effect_vein_miner",
            () -> new VeinMinerEffect(MobEffectCategory.BENEFICIAL, 0x2ddde3) );


    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION,
            ExoVeinMine.MODID);

    public static final DeferredHolder<Potion, Potion> POTION_VEIN_MINER = POTIONS.register("potion_vein_miner",
            () -> new Potion(new MobEffectInstance(EFFECT_VEIN_MINER, 6000, 0)));
    public static final DeferredHolder<Potion, Potion> POTION_VEIN_MINER_STRONG = POTIONS.register("potion_vein_miner_strong",
            () -> new Potion(new MobEffectInstance(EFFECT_VEIN_MINER, 6000, 1)));
    public static final DeferredHolder<Potion, Potion> POTION_VEIN_MINER_EXTENDED = POTIONS.register("potion_vein_miner_extended",
            () -> new Potion(new MobEffectInstance(EFFECT_VEIN_MINER, 16000, 0)));


    //Removed all sounds from the mod as they were more annoying than anything.
    /*public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,
            ArchVeinMiner.MOD_ID);

    public static final RegistryObject<SoundEvent> SOUND_VEIN_MINER_CHARGED = SOUNDS.register("vein_miner_charged",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ArchVeinMiner.MOD_ID, "vein_miner_charged")));
    public static final RegistryObject<SoundEvent> SOUND_VEIN_MINER_ON = SOUNDS.register("vein_miner_toggle_on",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ArchVeinMiner.MOD_ID, "vein_miner_toggle_on")));
    public static final RegistryObject<SoundEvent> SOUND_VEIN_MINER_OFF = SOUNDS.register("vein_miner_toggle_off",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ArchVeinMiner.MOD_ID, "vein_miner_toggle_off")));*/

    public static ResourceKey<Enchantment> VEIN_MINER_ENCHANTMENT = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "vein_miner")
    );


    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES,
            ExoVeinMine.MODID);

    public static final Supplier<AttachmentType<Double>> VEIN_MINER_DATA = ATTACHMENT_TYPES.register(
            "vein_miner_data", () -> AttachmentType.builder(() -> 0.0).serialize(Codec.DOUBLE).build()
    );


    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);
        //SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus()); //Removed all sounds from the mod as they were more annoying than anything.
    }
}
