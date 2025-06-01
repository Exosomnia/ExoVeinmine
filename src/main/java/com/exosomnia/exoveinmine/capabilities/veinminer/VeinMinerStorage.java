package com.exosomnia.exoveinmine.capabilities.veinminer;

import com.exosomnia.exoveinmine.Config;
import net.minecraft.nbt.DoubleTag;

public class VeinMinerStorage implements IVeinMinerStorage {

    private double charge;

    public VeinMinerStorage(double charge) {
        setCharge(charge);
    }

    @Override
    public void setCharge(double amount) { charge = Math.min(Math.max(0.0, amount), Config.maxCharge); }

    @Override
    public double getCharge() { return charge; }

    @Override
    public boolean isMax() { return charge >= Config.maxCharge; }

    @Override
    public DoubleTag serializeNBT() { return DoubleTag.valueOf(charge); }

    @Override
    public void deserializeNBT(DoubleTag nbt) {
        charge = nbt.getAsDouble();
    }
}
