package com.diamssword.characters.forge;

import net.minecraft.nbt.NbtCompound;

public interface SyncedCapability {
	public NbtCompound writeSyncData(boolean toOwner);
	public void applySyncData(NbtCompound tag);

}
