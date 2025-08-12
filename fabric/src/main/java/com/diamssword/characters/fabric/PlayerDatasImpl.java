package com.diamssword.characters.fabric;

import com.diamssword.characters.storage.ComponentManager;
import com.diamssword.characters.PlayerAppearance;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDatasImpl implements ComponentManager.IPlayerComponent, ComponentV3, ServerTickingComponent, AutoSyncedComponent {
	private final PlayerEntity player;
	private final PlayerAppearance appearance;

	public PlayerDatasImpl(PlayerEntity e) {
		this.player = e;
		this.appearance = new PlayerAppearance(e);
	}



	@Override
	public void serverTick() {
		this.appearance.tick();
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		if (tag.contains("appearance"))
			appearance.readFromNbt(tag.getCompound("appearance"));
	}

	@Override
	public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {

		NbtCompound tag = new NbtCompound();
			var ap = new NbtCompound();
			appearance.writeToNbt(ap, true);
			tag.put("appearance", ap);
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
		appearance.writeToNbt(ap, false);
		tag.put("appearance", ap);
	}

	@Override
	public PlayerAppearance getAppearence() {
		return this.appearance;
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
