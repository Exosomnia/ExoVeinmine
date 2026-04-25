package com.exosomnia.exoveinmine.datagen;

import com.exosomnia.exoveinmine.ExoVeinMine;
import com.exosomnia.exoveinmine.RegistrationHandler;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ExoVeinMine.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EnchantmentDataGen {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.ENCHANTMENT, bootstrap -> {
                HolderSet.Named<Item> items = bootstrap.lookup(Registries.ITEM).getOrThrow(ItemTags.MINING_ENCHANTABLE);

                bootstrap.register(RegistrationHandler.VEIN_MINER_ENCHANTMENT,
                        new Enchantment(
                                Component.translatable("enchantment.exoveinmine.vein_miner"),

                                new Enchantment.EnchantmentDefinition(
                                        items,
                                        Optional.of(items),
                                        2,
                                        1,
                                        Enchantment.constantCost(15),
                                        Enchantment.constantCost(65),
                                        4,
                                        List.of(EquipmentSlotGroup.MAINHAND)
                                ),

                                HolderSet.empty(),

                                DataComponentMap.EMPTY
                        ));
            });

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        event.createDatapackRegistryObjects(
                BUILDER,
                Set.of(ExoVeinMine.MODID)
        );
    }
}
