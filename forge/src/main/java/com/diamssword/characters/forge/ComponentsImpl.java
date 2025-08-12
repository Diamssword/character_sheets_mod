package com.diamssword.characters.forge;

import com.diamssword.characters.PlayerCharacters;
import com.diamssword.characters.network.SkinServerCache;
import com.diamssword.characters.storage.ComponentManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;

public class ComponentsImpl extends ComponentManager {
	@Override
	public <T, A> T getComponent(A entity, Class<T> componentClass) {
		if(entity instanceof PlayerEntity pl)
		{
			if( componentClass== IPlayerComponent.class)
				return (T) pl.getCapability(CapabilityEvents.PLAYER_APPEARANCE).resolve().get();
			else if( componentClass== PlayerCharacters.class)
				return (T) pl.getCapability(CapabilityEvents.PLAYER_CHARACTERS).resolve().get();
		}else if(entity instanceof MinecraftServer s && componentClass== SkinServerCache.class)
		{
			return (T) s.getOverworld().getCapability(CapabilityEvents.SKIN_SERVER_CACHE).resolve().get();
		}
		return null;
	}

	@Override
	public <T, A> void syncComponent(A entity, Class<T> componentClass) {
		if(entity instanceof ServerPlayerEntity pl)
		{
			if( componentClass== IPlayerComponent.class)
				pl.getCapability(CapabilityEvents.PLAYER_APPEARANCE).ifPresent(v->ModNetworking.syncToTracking(pl,new SyncPlayerStatsPacket(pl,CapabilityEvents.APPEARANCE)));
			else if( componentClass== PlayerCharacters.class)
				pl.getCapability(CapabilityEvents.PLAYER_CHARACTERS).ifPresent(v->ModNetworking.syncToClient(pl,new SyncPlayerStatsPacket(pl,CapabilityEvents.CHARACTERS)));
		}
	}

}
