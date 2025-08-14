package com.diamssword.characters.forge;

import com.diamssword.characters.storage.PlayerAppearance;
import com.diamssword.characters.api.IPlayerComponent;
import com.diamssword.characters.api.stats.IPlayerStats;
import com.diamssword.characters.storage.PlayerStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class PlayerDatasImpl implements IPlayerComponent, SyncedCapability  {
	private final PlayerEntity player;
	private final PlayerAppearance appearance;
	private final PlayerStats stats;

	public PlayerDatasImpl(PlayerEntity e) {
		this.player = e;
		this.appearance = new PlayerAppearance(e);
		this.stats = new PlayerStats(e);
	}



	public void serverTick() {
		this.appearance.tick();
	}

	public PlayerAppearance getAppearence() {
		return this.appearance;
	}

	@Override
	public IPlayerStats getStats() {
		return null;
	}

	public NbtCompound toNBT() {
		var res=new NbtCompound();
		var ap = new NbtCompound();
		appearance.writeToNbt(ap, 0);
		res.put("appearance", ap);
		return res;
	}

	public void fromNBT(NbtCompound nbt) {
		if (nbt.contains("appearance"))
			appearance.readFromNbt(nbt.getCompound("appearance"));
		if (nbt.contains("stats"))
			stats.readFromNbt(nbt.getCompound("stats"));
	}

	@Override
	public NbtCompound writeSyncData(boolean toOwner) {
		NbtCompound tag = new NbtCompound();
		var ap = new NbtCompound();
		appearance.writeToNbt(ap, 1);
		tag.put("appearance", ap);
		if(toOwner)
			tag.put("stats",stats.writeToNbt());
		return tag;
	}

	@Override
	public void applySyncData(NbtCompound tag) {
		if (tag != null) {
			this.fromNBT(tag);
		}
	}
}
