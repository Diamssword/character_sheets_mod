package com.diamssword.characters;

import com.diamssword.characters.api.ICharacterStored;
import com.diamssword.characters.api.http.ApiCharacterValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class InventorySaver implements ICharacterStored {
	private final ServerPlayerEntity player;

	public InventorySaver(ServerPlayerEntity player)
	{
		this.player=player;
	}
	@Override
	public void onCharacterLoad(PlayerEntity player, String newCharacterID, ApiCharacterValues newCharacter, @Nullable String oldCharacterID) {

	}
	public static NbtCompound serializer(InventorySaver instance)
	{
		var ls=new NbtList();
		instance.player.getInventory().writeNbt(ls);
		var t=new NbtCompound();
		t.put("inventory",ls);
		return t;
	}
	public static void unserializer(InventorySaver instance,NbtCompound tag)
	{
		if(tag.contains("inventory")) {
			instance.player.getInventory().readNbt(tag.getList("inventory", NbtElement.COMPOUND_TYPE));
		}
	}
}
