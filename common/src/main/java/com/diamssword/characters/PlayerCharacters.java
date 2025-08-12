package com.diamssword.characters;

import com.diamssword.characters.http.ApiCharacterValues;
import com.diamssword.characters.network.SkinServerCache;
import com.diamssword.characters.storage.ComponentManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PlayerCharacters {

	protected final PlayerEntity player;
	protected final Map<String, ApiCharacterValues> characters = new HashMap<>();
	protected final Map<String, NbtCompound> savedStats = new HashMap<>();
	protected final Map<String, NbtCompound> savedAppearence = new HashMap<>();
	protected final Map<String, NbtCompound> savedInventory = new HashMap<>();

	protected String currentCharID;
	protected ApiCharacterValues currentCharacter;

	public PlayerCharacters(PlayerEntity e) {
		this.player = e;
	}






	public Set<String> getCharactersNames() {
		return characters.keySet();
	}

	public ApiCharacterValues getCurrentCharacter() {
		return currentCharacter;
	}

	public String getCurrentCharacterID() {
		return currentCharID;
	}

	public void switchCharacter(String id) {
		var car = characters.get(id);
		var oldChar = currentCharID;
		if (car != null) {
			currentCharacter = car;
			currentCharID = id;
			var dt = ComponentManager.getPlayerDatas(player);
			var newAp = savedAppearence.remove(id);
			if (oldChar != null)
				savedAppearence.put(oldChar, dt.getAppearence().writeToNbt(new NbtCompound(), false));
			if (newAp != null) {
				dt.getAppearence().readFromNbt(newAp);
			}
		/*	var newSt = savedStats.remove(id);
			if (oldChar != null)
				savedStats.put(oldChar, dt.stats.write());
			if (newSt != null) {
				dt.stats.read(newSt);
			}*
			var inv = player.getComponent(Components.PLAYER_INVENTORY);
			var newInv = savedInventory.remove(id);
			if (oldChar != null)
				savedInventory.put(oldChar, inv.getInventory().toNBTComplete());
			if (newInv != null) {
				inv.getInventory().clearCache();
				inv.getInventory().fromNBTComplete(newInv, this.player);
			}
		 */
			SkinServerCache.get(player.getServer()).addToCache(player.getUuid(), car.base64Skin, car.base64SkinHead, car.appearence.slim);
			ComponentManager.syncPlayerCharacter(player);
			SkinServerCache.get(player.getServer()).setActiveCharacter(player, currentCharacter.stats.firstname + " " + currentCharacter.stats.lastname, currentCharacter.base64SkinHead);
		}
	}

	public void deleteCharacter(String id) {
		characters.remove(id);
		savedInventory.remove(id);
		savedStats.remove(id);
		savedAppearence.remove(id);
		if (id.equals(currentCharID)) {
			currentCharID = null;
		}
	}

	public String addNewCharacter(ApiCharacterValues character) {
		var id = character.stats.firstname.toLowerCase().replaceAll(" ", "") + "_" + character.stats.lastname.toLowerCase().replaceAll(" ", "");
		int i = 1;
		var itid = id;
		while (characters.containsKey(itid)) {
			itid = id + i;
			i++;
		}
		characters.put(itid, character);
		return itid;
	}
	protected  <T> NbtCompound mapToNBT(Map<String, T> map, Function<T, NbtCompound> provider) {
		var t1 = new NbtCompound();
		map.forEach((k, v) -> {
			t1.put(k, provider.apply(v));
		});
		return t1;
	}

	protected  <T> void NBTToMap(Map<String, T> map, NbtCompound tag, Function<NbtCompound, T> provider) {
		map.clear();
		tag.getKeys().forEach(k -> {
			map.put(k, provider.apply(tag.getCompound(k)));
		});
	}
	public void replaceCharacter(String id, ApiCharacterValues character) {
		characters.put(id, character);
	}
}
