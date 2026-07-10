package com.diamssword.characters.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class CharactersApi {
	public static final Identifier CHARACTER_ATTACHED_COMPONENT_INVENTORY=new Identifier("character_sheet","inventory");
	public static final Identifier CHARACTER_ATTACHED_COMPONENT_APPEARANCE=new Identifier("character_sheet","appearance");
	public static final Identifier CHARACTER_ATTACHED_COMPONENT_STATS=new Identifier("character_sheet","stats");
	private static CharactersApi instance;
	public static CharacterClothingApi clothing(){
		return instance.getClothing();
	}
	public static CharacterStatsApi stats(){
		return instance.getStats();
	}
	public static BodyPartsApi bodyParts(){
		return instance.getBodyParts();
	}
	private static List<Consumer<CharactersApi>> onReady=new ArrayList<>();
	@Environment(EnvType.CLIENT)
	public static CharacterSkinApi skin(){
		return instance.getSkins();
	}
	abstract protected CharacterClothingApi getClothing();
	abstract protected BodyPartsApi getBodyParts();
	abstract protected CharacterStatsApi getStats();
	@Environment(EnvType.CLIENT)
	abstract protected CharacterSkinApi getSkins();

	@Environment(EnvType.CLIENT)
	abstract public void overrideWardrobeGui(Function<String,Screen> factory);


	abstract public <T extends ICharacterStored> void attachComponentToCharacters(Identifier id, Function<ServerPlayerEntity,T> provider, Function<T, NbtCompound> serializer, BiConsumer<T, NbtCompound> unserializer);

	abstract public void unattachComponentFromCharacters(Identifier id);

	public static void onReady(Consumer<CharactersApi> callback)
	{
		if(instance!=null)
			callback.accept(instance);
		else
			onReady.add(callback);
	}
	public static CharactersApi get()
	{
		return instance;
	}
	public static void setReady(CharactersApi inst)
	{
		instance=inst;
		onReady.forEach(r->r.accept(inst));
		onReady.clear();
	}

}
