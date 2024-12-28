package com.exosomnia.exoveinmine.capabilities.veinminer;

import net.minecraft.nbt.FloatTag;

public class VeinMinerStorage implements IVeinMinerStorage {

    private float charge;

    public VeinMinerStorage(float charge) {
        setCharge(charge);
    }

    @Override
    public void setCharge(float amount) { charge = Math.min(amount, MAX_CHARGE); }

    @Override
    public float getCharge() { return charge; }

    @Override
    public boolean isMax() { return charge >= MAX_CHARGE; }

    @Override
    public FloatTag serializeNBT() { return FloatTag.valueOf(charge); }

    @Override
    public void deserializeNBT(FloatTag nbt) {
        charge = nbt.getAsFloat();
    }
}
