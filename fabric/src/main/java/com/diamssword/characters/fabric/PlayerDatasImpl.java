package com.diamssword.characters.fabric;

import com.diamssword.characters.storage.PlayerAppearance;
import com.diamssword.characters.api.IPlayerComponent;
import com.diamssword.characters.api.stats.IPlayerStats;
import com.diamssword.characters.storage.PlayerStats;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDatasImpl implements IPlayerComponent, ComponentV3, ServerTickingComponent, AutoSyncedComponent {
	private final PlayerEntity player;
	private final PlayerAppearance appearance;
	private final PlayerStats stats;

	public PlayerDatasImpl(PlayerEntity e) {
		this.player = e;
		this.appearance = new PlayerAppearance(e);
		this.stats = new PlayerStats(e);
	}



	@Override
	public void serverTick() {
		this.appearance.tick();
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		if (tag.contains("appearance"))
			appearance.readFromNbt(tag.getCompound("appearance"));
		if (tag.contains("stats"))
			stats.readFromNbt(tag.getCompound("stats"));
	}

	@Override
	public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {

		NbtCompound tag = new NbtCompound();
		var ap = new NbtCompound();
		appearance.writeToNbt(ap, 1);
		tag.put("appearance", ap);
		if(recipient==this.player)
			tag.put("stats",stats.writeToNbt());
		buf.writeNbt(tag);
	}

	@Override
	public void applySyncPacket(PacketByteBuf buf) {
		NbtCompound tag = buf.readNbt();
		if (tag != null) {
			this.readFromNbt(tag);
		}
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		var ap = new NbtCompound();
		appearance.writeToNbt(ap, 0);
		tag.put("appearance", ap);
		tag.put("stats", stats.writeToNbt());
	}

	@Override
	public PlayerAppearance getAppearence() {
		return this.appearance;
	}


	@Override
	public IPlayerStats getStats() {
		return stats;
	}
	@Override
	public NbtCompound toNBT() {
		var res=new NbtCompound();
		writeToNbt(res);
		return res;
	}

	@Override
	public void fromNBT(NbtCompound nbt) {
		this.readFromNbt(nbt);
	}
}
