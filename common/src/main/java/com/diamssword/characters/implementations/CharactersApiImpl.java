package com.diamssword.characters.implementations;

import com.diamssword.characters.api.CharacterClothingApi;
import com.diamssword.characters.api.CharacterStatsApi;
import com.diamssword.characters.api.CharactersApi;

public class CharactersApiImpl extends CharactersApi {
	private final CharactersClothingApiImpl cloth=new CharactersClothingApiImpl();
	private final CharactersStatsApiImpl stats=new CharactersStatsApiImpl();
	@Override
	protected CharacterClothingApi getClothing() {
		return cloth;
	}

	@Override
	protected CharacterStatsApi getStats() {
		return stats;
	}
}
