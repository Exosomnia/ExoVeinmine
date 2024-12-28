package com.exosomnia.exoveinmine.capabilities.veinminer;

import net.minecraft.core.Direction;
import net.minecraft.nbt.FloatTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class VeinMinerProvider implements ICapabilitySerializable<FloatTag> {

    public static final Capability<IVeinMinerStorage> VEIN_MINER = CapabilityManager.get(new CapabilityToken<>(){});
    private final LazyOptional<IVeinMinerStorage> instance = LazyOptional.of(() -> new VeinMinerStorage(0.0F));

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == VEIN_MINER ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public FloatTag serializeNBT() { return instance.resolve().get().serializeNBT(); }

    @Override
    public void deserializeNBT(FloatTag nbt) { instance.resolve().get().deserializeNBT(nbt); }
}
