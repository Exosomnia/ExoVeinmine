package com.exosomnia.exoveinmine.effects;

import com.exosomnia.exoveinmine.ExoVeinMine;
import com.exosomnia.exoveinmine.RegistrationHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class VeinMinerEffect extends MobEffect {

    private final static ResourceLocation EFFICIENCY_UUID =
            ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "vein_mine_efficiency");
    private final static ResourceLocation CHARGE_UUID =
            ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "vein_mine_charge");

    public VeinMinerEffect(MobEffectCategory typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);

        this.addAttributeModifier(RegistrationHandler.VEIN_MINER_EFFICIENCY,
                EFFICIENCY_UUID, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(RegistrationHandler.VEIN_MINER_CHARGE,
                CHARGE_UUID, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
