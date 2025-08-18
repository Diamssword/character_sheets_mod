package com.diamssword.characters.implementations;

import com.diamssword.characters.api.*;
import com.diamssword.characters.storage.PlayerCharacters;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Function;

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

	@Override
	@Environment(EnvType.CLIENT)
	protected CharacterSkinApi getSkins() {
		return new CharacterSkinImpl();
	}

	@Override
	public <T extends ICharacterStored> void attachComponentToCharacters(Identifier id, Function<ServerPlayerEntity, T> provider, Function<T, NbtCompound> serializer, BiConsumer<T, NbtCompound> unserializer) {
		PlayerCharacters.attachComponentToCharacters(id,provider,serializer,unserializer);
	}

	@Override
	public void unattachComponentFromCharacters(Identifier id) {
		PlayerCharacters.unattachComponentFromCharacters(id);
	}
}
