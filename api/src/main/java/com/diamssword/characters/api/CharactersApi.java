package com.diamssword.characters.api;

import net.minecraft.util.Identifier;

public abstract class CharactersApi {
	public static final Identifier CHARACTER_ATTACHED_COMPONENT_INVENTORY=new Identifier("character_sheet","inventory");
	public static final Identifier CHARACTER_ATTACHED_COMPONENT_APPEARANCE=new Identifier("character_sheet","appearance");
	public static final Identifier CHARACTER_ATTACHED_COMPONENT_STATS=new Identifier("character_sheet","stats");
	public static CharactersApi instance;
	public static CharacterClothingApi clothing(){
		return instance.getClothing();
	}
	public static CharacterStatsApi stats(){
		return instance.getStats();
	}
	abstract protected CharacterClothingApi getClothing();
	abstract protected CharacterStatsApi getStats();

}
