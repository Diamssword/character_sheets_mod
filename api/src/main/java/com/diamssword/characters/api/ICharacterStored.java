package com.diamssword.characters.api;

import com.diamssword.characters.api.http.ApiCharacterValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ICharacterStored {
	/** Called when a character sheet is loaded for the player
	 * @param player
	 * @param newCharacterID
	 * @param newCharacter
	 * @param oldCharacterID
	 */
	public void onCharacterLoad(PlayerEntity player,String newCharacterID, ApiCharacterValues newCharacter, @Nullable String oldCharacterID);
}
