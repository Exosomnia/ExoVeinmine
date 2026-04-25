package com.exosomnia.exoveinmine.util;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.LevelAccessor;

import java.util.Set;
import java.util.function.BiPredicate;

public class VeinMineHelper {

    public record Requirement(BiPredicate<Player, LevelAccessor> predicate, String error) {}

    private static Set<Requirement> ACCESS_REQUIREMENTS;
    private static Set<BiPredicate<Player, LevelAccessor>> ACCESS_CHECKS;
    private static Set<BiPredicate<Player, LevelAccessor>> ENHANCED_CHECKS;

    public static final Requirement TOOL_REQUIRED_PREDICATE = new Requirement(
            (player, level) -> {
                var mainhand = player.getMainHandItem().getItem();
                return ( (mainhand instanceof DiggerItem) || (mainhand instanceof ShearsItem) );
            },
            "error.exoveinmine.tool_required"
    );
    public static final BiPredicate<Player, LevelAccessor> HAS_ACCESS_TAG =
            (player, level) -> player.getTags().contains(Config.tagName);
    public static final BiPredicate<Player, LevelAccessor> HAS_ENHANCED_TAG =
            (player, level) -> player.getTags().contains(Config.tagNameEnhanced);
    public static final BiPredicate<Player, LevelAccessor> HAS_ENCHANTMENT =
            (player, level) -> {
                var registry = level.registryAccess().registry(Registries.ENCHANTMENT);
                if (registry.isEmpty()) return false;

                var enchantment = registry.get().getHolder(RegistrationHandler.VEIN_MINER_ENCHANTMENT);
                if (enchantment.isEmpty()) return false;

                return player.getMainHandItem().getEnchantmentLevel(enchantment.get()) > 0;
            };

    public static void setPredicates(Set<Requirement> requirements, Set<BiPredicate<Player, LevelAccessor>> access, Set<BiPredicate<Player, LevelAccessor>> enhanced) {
        ACCESS_REQUIREMENTS = ImmutableSet.copyOf(requirements);
        ACCESS_CHECKS = ImmutableSet.copyOf(access);
        ENHANCED_CHECKS = ImmutableSet.copyOf(enhanced);
    }

    public static Requirement checkRequirements(Player player, LevelAccessor level) {
        for (Requirement requirement : ACCESS_REQUIREMENTS) {
            if (!requirement.predicate().test(player, level)) return requirement;
        }

        return null;
    }

    public static boolean checkAccess(Player player, LevelAccessor level) {
        if (ACCESS_CHECKS.isEmpty()) return true;
        for (BiPredicate<Player, LevelAccessor> requirement : ACCESS_CHECKS) {
            if (requirement.test(player, level)) return true;
        }

        return false;
    }

    public static boolean checkEnhanced(Player player, LevelAccessor level) {
        if (ENHANCED_CHECKS.isEmpty()) return true;
        for (BiPredicate<Player, LevelAccessor> requirement : ENHANCED_CHECKS) {
            if (requirement.test(player, level)) return true;
        }

        return false;
    }

    public static boolean hasEnchantment(ItemStack item, LevelAccessor level) {
        var registry = level.registryAccess().registry(Registries.ENCHANTMENT);
        if (registry.isEmpty()) return false;

        var enchantment = registry.get().getHolder(RegistrationHandler.VEIN_MINER_ENCHANTMENT);
        if (enchantment.isEmpty()) return false;

        return item.getEnchantmentLevel(enchantment.get()) > 0;
    }
}
