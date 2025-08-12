package com.diamssword.characters.fabric;

import com.diamssword.characters.Characters;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;

public class Components implements EntityComponentInitializer, LevelComponentInitializer {
	public static final ComponentKey<PlayerDatasImpl> PLAYER_DATAS = ComponentRegistry.getOrCreate(Characters.asRessource("appearance"), PlayerDatasImpl.class);
	public static final ComponentKey<PlayerCharactersImpl> PLAYER_CHARACTERS = ComponentRegistry.getOrCreate(Characters.asRessource("characters"), PlayerCharactersImpl.class);
	public static final ComponentKey<SkinServerCacheImpl> SERVER_PLAYER_CACHE = ComponentRegistryV3.INSTANCE.getOrCreate(Characters.asRessource("player_cache"), SkinServerCacheImpl.class);
	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(PLAYER_DATAS, PlayerDatasImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(PLAYER_CHARACTERS, PlayerCharactersImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
	}

	@Override
	public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
		registry.register(SERVER_PLAYER_CACHE, (e)->new SkinServerCacheImpl());
	}
}
