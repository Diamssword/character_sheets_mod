package com.diamssword.characters.fabric;

import com.diamssword.characters.storage.PlayerCharacters;
import com.diamssword.characters.api.http.ApiCharacterValues;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerCharactersImpl extends PlayerCharacters implements ComponentV3, AutoSyncedComponent {

	public PlayerCharactersImpl(PlayerEntity e) {
		super(e);
	}
	@Override
	public void readFromNbt(NbtCompound tag) {
		if (tag.contains("characters")) {
			characters.clear();
			var t1=tag.getCompound("characters");
			t1.getKeys().forEach(k -> {
				var d = new ApiCharacterValues();
				d.charactersfromNBT(t1.getCompound(k));
				characters.put(k,d);
			});
			if (tag.contains("data")) {
				storedDatas.clear();
				var t2=tag.getCompound("data");
				t2.getKeys().forEach(k -> storedDatas.put(k,t2.getCompound(k)));
			}
		}
		if (tag.contains("current")) {
			currentCharID = tag.getString("current");
			currentCharacter = characters.get(currentCharID);
		}
	}
	@Override
	public void writeToNbt(NbtCompound tag) {
		var t1 = new NbtCompound();
		characters.forEach((k, v) -> t1.put(k, v.toNBT()));
		tag.put("characters", t1);
		var t2 = new NbtCompound();
		storedDatas.forEach(t2::put);
		tag.put("data",t2);
		if (currentCharID != null)
			tag.putString("current", currentCharID);
	}
	@Override
	public boolean shouldSyncWith(ServerPlayerEntity player) {
		return player == this.player;
	}

	@Override
	public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
		NbtCompound tag = new NbtCompound();
		if (this.currentCharacter != null) {
			tag.put("current", this.currentCharacter.toNBT());
			tag.putString("currentID", this.currentCharID);
		}
		buf.writeNbt(tag);
	}

	@Override
	public void applySyncPacket(PacketByteBuf buf) {
		NbtCompound tag = buf.readNbt();
		if (tag != null) {
			if (tag.contains("current")) {
				var d = new ApiCharacterValues();
				d.charactersfromNBT(tag.getCompound("current"));
				currentCharacter = d;
				this.currentCharID = tag.getString("currentID");
			}
		}
	}

}
