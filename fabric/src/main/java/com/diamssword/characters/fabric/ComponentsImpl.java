package com.diamssword.characters.fabric;

import com.diamssword.characters.PlayerCharacters;
import com.diamssword.characters.network.SkinServerCache;
import com.diamssword.characters.storage.ComponentManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.level.ServerWorldProperties;

public class ComponentsImpl extends ComponentManager {
	@Override
	public <T, A> T getComponent(A entity, Class<T> componentClass) {
		if(entity instanceof PlayerEntity pl)
		{
			if( componentClass== ComponentManager.IPlayerComponent.class)
				return (T) pl.getComponent(Components.PLAYER_DATAS);
			if( componentClass== PlayerCharacters.class)
				return (T) pl.getComponent(Components.PLAYER_CHARACTERS);
		}else if(entity instanceof  MinecraftServer w && componentClass== SkinServerCache.class)
		{
			return (T) w.getSaveProperties().getMainWorldProperties().getComponent(Components.SERVER_PLAYER_CACHE);
		}
		return null;
	}

	@Override
	public <T, A> void syncComponent(A entity, Class<T> componentClass) {
		if(entity instanceof ServerPlayerEntity pl)
		{

			if( componentClass== ComponentManager.IPlayerComponent.class)
				pl.syncComponent(Components.PLAYER_DATAS);
			if( componentClass== PlayerCharacters.class)
				pl.syncComponent(Components.PLAYER_CHARACTERS);
		}
	}

}
