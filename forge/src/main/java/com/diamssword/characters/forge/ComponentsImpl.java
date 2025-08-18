package com.diamssword.characters.forge;

import com.diamssword.characters.api.ICharacterSheets;
import com.diamssword.characters.api.IPlayerComponent;
import com.diamssword.characters.network.SkinServerCache;
import com.diamssword.characters.api.ComponentManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ComponentsImpl extends ComponentManager {

	@Override
	public <T, A> T getComponent(A entity, Class<T> componentClass) {
		if(entity instanceof PlayerEntity pl)
		{
			if( componentClass== IPlayerComponent.class)
				return (T) pl.getCapability(CapabilityEvents.PLAYER_APPEARANCE).resolve().get();
			else if( componentClass== ICharacterSheets.class)
				return (T) pl.getCapability(CapabilityEvents.PLAYER_CHARACTERS).resolve().get();
		}else if(entity instanceof MinecraftServer s && componentClass== SkinServerCache.class)
		{
			return (T) s.getOverworld().getCapability(CapabilityEvents.SKIN_SERVER_CACHE).resolve().get();
		}
		return null;
	}

	@Override
	public <T, A> void syncComponent(A entity, Class<T> componentClass) {
		if(entity instanceof ServerPlayerEntity pl && pl.networkHandler !=null)
		{
			if( componentClass== IPlayerComponent.class) {
				pl.getCapability(CapabilityEvents.PLAYER_APPEARANCE).ifPresent(v -> ModNetworking.syncToTracking(pl, new SyncPlayerStatsPacket(pl, CapabilityEvents.APPEARANCE,false)));
				pl.getCapability(CapabilityEvents.PLAYER_APPEARANCE).ifPresent(v -> ModNetworking.syncToClient(pl, new SyncPlayerStatsPacket(pl, CapabilityEvents.APPEARANCE,true)));
			}
			else if( componentClass== ICharacterSheets.class)
				pl.getCapability(CapabilityEvents.PLAYER_CHARACTERS).ifPresent(v->ModNetworking.syncToClient(pl,new SyncPlayerStatsPacket(pl,CapabilityEvents.CHARACTERS,true)));
		}
	}

}
