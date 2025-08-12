package com.diamssword.characters.forge;

import com.diamssword.characters.http.ApiCharacterValues;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public interface SyncedCapability {
	public NbtCompound writeSyncData();
	public void applySyncData(NbtCompound tag);

}
