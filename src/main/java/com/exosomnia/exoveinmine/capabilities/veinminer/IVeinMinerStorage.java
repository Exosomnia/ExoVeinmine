package com.exosomnia.exoveinmine.capabilities.veinminer;

import com.exosomnia.exoveinmine.Config;
import net.minecraft.nbt.DoubleTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IVeinMinerStorage extends INBTSerializable<DoubleTag> {

    double MAX_CHARGE = Config.maxCharge;
    double CHARGE_PER_BLOCK = Config.chargePerBlock;

    /**
     * Sets the specified amount to the player's vein miner charge.
     * @param amount the amount of charge to set.
     */
    void setCharge(double amount);

    /**
     * Gets the current charge level of the player.
     * @return the player's current charge level.
     */
    double getCharge();

    /**
     * Returns if the charge is at, or above the maximum charge.
     * @return true/false if the charge is at or above max.
     */
    boolean isMax();
}
