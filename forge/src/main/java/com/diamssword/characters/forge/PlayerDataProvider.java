package com.diamssword.characters.forge;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerDataProvider implements ICapabilityProvider, INBTSerializable<NbtCompound> {



    private final LazyOptional<PlayerDatasImpl> instance;
    public PlayerDataProvider(PlayerEntity e)
    {
        instance= LazyOptional.of(()->new PlayerDatasImpl(e));
    }
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == CapabilityEvents.PLAYER_APPEARANCE ? instance.cast() : LazyOptional.empty();
    }
    
    @Override
    public NbtCompound serializeNBT() {
        return instance.map(PlayerDatasImpl::toNBT).orElse(new NbtCompound());
    }
    @Override
    public void deserializeNBT(NbtCompound nbt) {
        instance.ifPresent(stats -> stats.fromNBT(nbt));
    }
    
    public void invalidate() {
        instance.invalidate();
    }
}