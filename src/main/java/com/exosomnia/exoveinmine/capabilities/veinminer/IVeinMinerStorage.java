package com.exosomnia.exoveinmine.capabilities.veinminer;

import com.exosomnia.exoveinmine.Config;
import net.minecraft.nbt.FloatTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IVeinMinerStorage extends INBTSerializable<FloatTag> {

    float MAX_CHARGE = Config.maxCharge;
    float DEFAULT_INCREMENT = Config.defaultIncrement;

    /**
     * Sets the specified amount to the player's vein miner charge.
     * @param amount the amount of charge to set.
     */
    void setCharge(float amount);

    /**
     * Gets the current charge level of the player.
     * @return the player's current charge level.
     */
    float getCharge();

    /**
     * Returns if the charge is at, or above the maximum charge.
     * @return true/false if the charge is at or above max.
     */
    boolean isMax();
}
