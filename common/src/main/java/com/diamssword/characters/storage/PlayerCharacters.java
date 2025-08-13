package com.diamssword.characters.storage;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.ICharacterSheets;
import com.diamssword.characters.api.ICharacterStored;
import com.diamssword.characters.api.http.ApiCharacterValues;
import com.diamssword.characters.network.SkinServerCache;
import com.diamssword.characters.api.ComponentManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlayerCharacters implements ICharacterSheets {

	private static final Map<Identifier,StorageGlobal> globalRegister=new HashMap<>();
	protected record StorageGlobal(Function<ServerPlayerEntity,ICharacterStored> provider, Function<ICharacterStored, NbtCompound> serializer, BiConsumer<ICharacterStored, NbtCompound> unserializer){}
	protected record Storage(Supplier<ICharacterStored> instance,Supplier<NbtCompound> serializer, Consumer<NbtCompound> unserializer){}
	protected final PlayerEntity player;
	protected final Map<Identifier,Storage> registered=new HashMap<>();
	protected final Map<String, ApiCharacterValues> characters = new HashMap<>();
	protected final Map<String,NbtCompound> storedDatas=new HashMap<>();

	protected String currentCharID;
	protected ApiCharacterValues currentCharacter;

	public PlayerCharacters(PlayerEntity e) {
		this.player = e;
		if(e instanceof ServerPlayerEntity pl) {
			globalRegister.forEach((k, v) -> {
				this.attachComponent(k, ()->v.provider.apply(pl), () -> v.serializer.apply(v.provider.apply(pl)),(t)->v.unserializer.accept(v.provider.apply(pl),t));
			});
		}
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
			var oldCharSave=new NbtCompound();
			var newStore=storedDatas.getOrDefault(id,new NbtCompound());
			registered.forEach((i,s)->{
				if(oldChar !=null) {
					oldCharSave.put(i.toString(),s.serializer.get());
				}
				if(newStore.contains(i.toString()))
					s.unserializer.accept(newStore.getCompound(i.toString()));
				s.instance.get().onCharacterLoad(player,id,car,oldChar);
			});
			if(oldChar!=null)
				storedDatas.put(oldChar,oldCharSave);
			SkinServerCache.get(player.getServer()).addToCache(player.getUuid(), car.base64Skin, car.base64SkinHead, car.appearence.slim);
			ComponentManager.syncPlayerCharacter(player);
			SkinServerCache.get(player.getServer()).setActiveCharacter(player, currentCharacter.stats.firstname + " " + currentCharacter.stats.lastname, currentCharacter.base64SkinHead);
		}
	}

	public void deleteCharacter(String id) {
		characters.remove(id);
		storedDatas.remove(id);
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
	public void replaceCharacter(String id, ApiCharacterValues character) {
		characters.put(id, character);
	}

	@Override
	public void attachComponent(Identifier id, Supplier<ICharacterStored> instance, Supplier<NbtCompound> serializer, Consumer<NbtCompound> unserializer) {
		this.registered.put(id,new Storage(instance,serializer,unserializer));
	}

	@Override
	public void unattachComponent(Identifier id) {
		this.registered.remove(id);
	}
	public static <T extends ICharacterStored> void attachComponentToCharacters(Identifier id, Function<ServerPlayerEntity,T> provider, Function<T, NbtCompound> serializer, BiConsumer<T, NbtCompound> unserializer) {
		globalRegister.put(id,new StorageGlobal((Function<ServerPlayerEntity, ICharacterStored>) provider, (Function<ICharacterStored, NbtCompound>) serializer, (BiConsumer<ICharacterStored, NbtCompound>) unserializer));
		Characters.LOGGER.info("Attached Character Sheet Component: {}",id);
	}

	public static void unattachComponentFromCharacters(Identifier id) {
		globalRegister.remove(id);
	}
}
