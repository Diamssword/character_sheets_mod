package com.diamssword.characters.forge;

import com.diamssword.characters.PlayerAppearance;
import com.diamssword.characters.storage.ComponentManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDatasImpl implements ComponentManager.IPlayerComponent,SyncedCapability  {
	private final PlayerEntity player;
	private final PlayerAppearance appearance;

	public PlayerDatasImpl(PlayerEntity e) {
		this.player = e;
		this.appearance = new PlayerAppearance(e);
	}



	public void serverTick() {
		this.appearance.tick();
	}

	public PlayerAppearance getAppearence() {
		return this.appearance;
	}

	public NbtCompound toNBT() {
		var res=new NbtCompound();
		var ap = new NbtCompound();
		appearance.writeToNbt(ap, false);
		res.put("appearance", ap);
		return res;
	}

	public void fromNBT(NbtCompound nbt) {
		if (nbt.contains("appearance"))
			appearance.readFromNbt(nbt.getCompound("appearance"));
	}

	@Override
	public NbtCompound writeSyncData() {
		NbtCompound tag = new NbtCompound();
		var ap = new NbtCompound();
		appearance.writeToNbt(ap, true);
		tag.put("appearance", ap);
		return tag;
	}

	@Override
	public void applySyncData(NbtCompound tag) {
		if (tag != null) {
			this.fromNBT(tag);
		}
	}
}
