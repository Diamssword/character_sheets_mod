package com.diamssword.characters.forge;

import com.diamssword.characters.PlayerCharacters;
import com.diamssword.characters.http.ApiCharacterValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.function.Function;

public class PlayerCharactersImpl extends PlayerCharacters implements SyncedCapability{

	public PlayerCharactersImpl(PlayerEntity e) {
		super(e);
	}
	public void readFromNbt(NbtCompound tag) {
		if (tag.contains("characters")) {
			NBTToMap(characters, tag.getCompound("characters"), t -> {
				var d = new ApiCharacterValues();
				return d.charactersfromNBT(t);
			});
			NBTToMap(savedAppearence, tag.getCompound("appearance"), t -> t);
			NBTToMap(savedStats, tag.getCompound("stats"), t -> t);
			NBTToMap(savedInventory, tag.getCompound("inventory"), t -> t);
		}
		if (tag.contains("current")) {
			currentCharID = tag.getString("current");
			currentCharacter = characters.get(currentCharID);
		}
	}
	public NbtCompound toNBT() {
		NbtCompound tag=new NbtCompound();
		tag.put("characters", mapToNBT(characters, ApiCharacterValues::toNBT));
		tag.put("stats", mapToNBT(savedStats, t -> t));
		tag.put("appearance", mapToNBT(savedAppearence, t -> t));
		tag.put("inventory", mapToNBT(savedInventory, t -> t));
		if (currentCharID != null)
			tag.putString("current", currentCharID);
		return tag;
	}

	public boolean shouldSyncWith(ServerPlayerEntity player) {
		return player == this.player;
	}

	public NbtCompound writeSyncData() {
		NbtCompound tag = new NbtCompound();
		if (this.currentCharacter != null) {
			tag.put("current", this.currentCharacter.toNBT());
			tag.putString("currentID", this.currentCharID);
		}
		return tag;
	}
	public void applySyncData(NbtCompound tag) {
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
