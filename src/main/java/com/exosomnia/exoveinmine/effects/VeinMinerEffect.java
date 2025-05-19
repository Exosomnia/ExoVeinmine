package com.exosomnia.exoveinmine.effects;

import com.exosomnia.exoveinmine.RegistrationHandler;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class VeinMinerEffect extends MobEffect {

    private final static String EFFICIENCY_UUID = "f7625153-9677-46ec-ae50-afabd04f957c";
    private final static String CHARGE_UUID = "dbf06f55-fbaa-45d4-852d-6e4af0434c18";

    public VeinMinerEffect(MobEffectCategory typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);

        this.addAttributeModifier(RegistrationHandler.VEIN_MINER_EFFICIENCY.get(),
                EFFICIENCY_UUID, 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(RegistrationHandler.VEIN_MINER_CHARGE.get(),
                CHARGE_UUID, 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
