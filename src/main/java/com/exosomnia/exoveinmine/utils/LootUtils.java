package com.exosomnia.exoveinmine.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Random;

public class LootUtils {

    public static Random random = new Random();

    public static ItemStack applyFortune(ItemStack drops, ItemStack tool) {
        int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
        if (fortuneLevel > 0) {
            int i = random.nextInt(fortuneLevel + 2) - 1;
            if (i < 0) {
                i = 0;
            }

            drops.setCount(drops.getCount() * (i + 1));
            return drops;
        } else {
            return drops;
        }
    }
}
