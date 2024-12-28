package com.exosomnia.exoveinmine.brewing.recipes;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class SimpleBrewingRecipe implements IBrewingRecipe {

    private final Potion potion;
    private final Item ingredient;
    private final Potion output;

    public SimpleBrewingRecipe(Potion potion, Item ingredient, Potion outputs) {
        this.potion = potion;
        this.ingredient = ingredient;
        this.output = outputs;
    }

    @Override
    public boolean isInput(ItemStack input) {
        return input.getItem() == Items.POTION && PotionUtils.getPotion(input) == potion;
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.getItem() == this.ingredient;
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (isInput(input) && isIngredient(ingredient)) {
            ItemStack output = new ItemStack(Items.POTION);
            PotionUtils.setPotion(output, this.output);
            return output;
        }
        return ItemStack.EMPTY;
    }
}
