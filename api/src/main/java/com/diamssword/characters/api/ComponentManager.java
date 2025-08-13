package com.diamssword.characters.api;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

public abstract class ComponentManager {
	public static ComponentManager INSTANCE;
	public abstract <T,A> T getComponent(A entity, Class<T> componentClass);
	public abstract <T,A> void syncComponent(A entity, Class<T> componentClass);
	// Convenience methods
	public static IPlayerComponent getPlayerDatas(PlayerEntity player) {
		return INSTANCE.getComponent(player,IPlayerComponent.class);
	}
	public static ICharacterSheets getPlayerCharacter(PlayerEntity player) {
		return INSTANCE.getComponent(player,ICharacterSheets.class);
	}
	public static void syncPlayerDatas(PlayerEntity player) {
		INSTANCE.syncComponent(player, IPlayerComponent.class);
	}
	public static void syncPlayerCharacter(PlayerEntity player) {
		INSTANCE.syncComponent(player,ICharacterSheets.class);
	}

}
