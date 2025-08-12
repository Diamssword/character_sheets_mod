package com.diamssword.characters.forge;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SkinServerCacheProvider implements ICapabilityProvider, INBTSerializable<NbtCompound> {
    private final LazyOptional<SkinServerCacheImpl>   instance= LazyOptional.of(SkinServerCacheImpl::new);
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == CapabilityEvents.SKIN_SERVER_CACHE ? instance.cast() : LazyOptional.empty();
    }
    
    @Override
    public NbtCompound serializeNBT() {
        return instance.map(SkinServerCacheImpl::toNBT).orElse(new NbtCompound());
    }
    @Override
    public void deserializeNBT(NbtCompound nbt) {
        instance.ifPresent(stats -> stats.readFromNbt(nbt));
    }
    
    public void invalidate() {
        instance.invalidate();
    }
}