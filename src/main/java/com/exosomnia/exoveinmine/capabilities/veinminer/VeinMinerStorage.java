package com.exosomnia.exoveinmine.capabilities.veinminer;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;

public class VeinMinerStorage implements IVeinMinerStorage {

    private double charge;

    public VeinMinerStorage(double charge) {
        setCharge(charge);
    }

    @Override
    public void setCharge(double amount) { charge = Math.min(amount, MAX_CHARGE); }

    @Override
    public double getCharge() { return charge; }

    @Override
    public boolean isMax() { return charge >= MAX_CHARGE; }

    @Override
    public DoubleTag serializeNBT() { return DoubleTag.valueOf(charge); }

    @Override
    public void deserializeNBT(DoubleTag nbt) {
        charge = nbt.getAsDouble();
    }
}
