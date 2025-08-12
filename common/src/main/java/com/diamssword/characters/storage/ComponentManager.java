package com.diamssword.characters.storage;

import com.diamssword.characters.PlayerAppearance;
import com.diamssword.characters.PlayerCharacters;
import com.diamssword.characters.network.SkinServerCache;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ServerWorldProperties;

public abstract class ComponentManager {
	public static ComponentManager INSTANCE;

	public static SkinServerCache getSkinServerCache(MinecraftServer server) {
		return INSTANCE.getComponent(server,SkinServerCache.class);
	}

	public abstract <T,A> T getComponent(A entity, Class<T> componentClass);
	public abstract <T,A> void syncComponent(A entity, Class<T> componentClass);
	// Convenience methods
	public static IPlayerComponent getPlayerDatas(PlayerEntity player) {
		return INSTANCE.getComponent(player,IPlayerComponent.class);
	}
	public static PlayerCharacters getPlayerCharacter(PlayerEntity player) {
		return INSTANCE.getComponent(player,PlayerCharacters.class);
	}
	public static void syncPlayerDatas(PlayerEntity player) {
		INSTANCE.syncComponent(player,IPlayerComponent.class);
	}
	public static void syncPlayerCharacter(PlayerEntity player) {
		INSTANCE.syncComponent(player,PlayerCharacters.class);
	}
	public static interface IPlayerComponent{
		public PlayerAppearance getAppearence();
		public NbtCompound toNBT();
		public void fromNBT(NbtCompound nbt);
	}
}
