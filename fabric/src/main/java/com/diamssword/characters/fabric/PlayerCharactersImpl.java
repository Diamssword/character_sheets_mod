package com.diamssword.characters.fabric;

import com.diamssword.characters.PlayerCharacters;
import com.diamssword.characters.http.ApiCharacterValues;
import com.diamssword.characters.network.SkinServerCache;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.WorldProperties;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class PlayerCharactersImpl extends PlayerCharacters implements ComponentV3, AutoSyncedComponent {

	public PlayerCharactersImpl(PlayerEntity e) {
		super(e);
	}
	@Override
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
	@Override
	public void writeToNbt(NbtCompound tag) {
		tag.put("characters", mapToNBT(characters, ApiCharacterValues::toNBT));
		tag.put("stats", mapToNBT(savedStats, t -> t));
		tag.put("appearance", mapToNBT(savedAppearence, t -> t));
		tag.put("inventory", mapToNBT(savedInventory, t -> t));
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
