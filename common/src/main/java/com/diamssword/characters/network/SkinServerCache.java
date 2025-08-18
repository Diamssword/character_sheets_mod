package com.diamssword.characters.network;

import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.PlayerPresence;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.stream.Collectors;

public class SkinServerCache {


	public record PlayerInfos(String skin, String head, boolean slim) {
	}


	public record SendPlayerPresences(Map<UUID, PlayerPresence> presences) {
	}

	private final Map<UUID, PlayerInfos> skinCache = new HashMap<>();
	protected final Map<UUID, PlayerPresence> existingPlayers = new HashMap<>();

	public record SendPlayerInfos(UUID player, String skin, String skinHead, boolean slim) {
	}

	public record RequestPlayerInfos(UUID player) {
	}

	public record RequestPlayerPresence(UUID player) {
	}

	public record RequestPlayersMatching(String query) {
	}

	public SkinServerCache() {

	}

	public Optional<PlayerInfos> getSkin(UUID user) {
		return Optional.ofNullable(skinCache.get(user));
	}

	public void addToCache(UUID id, String skin, String head, boolean slim) {
		skinCache.put(id, new PlayerInfos(skin, head, slim));
	}

	public void removeFromCache(UUID uuid) {
		skinCache.remove(uuid);
	}
	public PlayerInfos get(UUID id)
	{
		return skinCache.get(id);
	}
	public static void init() {
		Channels.MAIN.registerClientboundDeferred(SendPlayerInfos.class);
		Channels.MAIN.registerClientboundDeferred(SendPlayerPresences.class);
		Channels.MAIN.registerServerbound(RequestPlayersMatching.class, (msg, ctx) -> {
			Channels.MAIN.serverHandle(ctx.player()).send(new SendPlayerPresences(SkinServerCache.get(ctx.player().server).getPlayersMatching(msg.query)));

		});
		Channels.MAIN.registerServerbound(RequestPlayerPresence.class, (msg, ctx) -> {
			var dt = SkinServerCache.get(ctx.player().server).existingPlayers.get(msg.player);
			var m = new HashMap<UUID, PlayerPresence>();
			if (dt != null)
				m.put(msg.player, dt);
			Channels.MAIN.serverHandle(ctx.player()).send(new SendPlayerPresences(m));
		});
		Channels.MAIN.registerServerbound(RequestPlayerInfos.class, (msg, ctx) -> {

			var skin = SkinServerCache.get(ctx.player().server).skinCache.get(msg.player);
			if (skin != null)
				Channels.MAIN.serverHandle(ctx.player()).send(new SendPlayerInfos(msg.player, skin.skin, skin.head, skin.slim));
		});
	}

	public void setActiveCharacter(PlayerEntity player, String characterName, String headTexture) {
		existingPlayers.put(player.getUuid(), new PlayerPresence(characterName, player.getGameProfile().getName(), headTexture));
	}

	public Map<UUID, PlayerPresence> getPlayersMatching(String query) {
		String lowerQuery = query.toLowerCase();

		// First pass: matches by username
		Map<UUID, PlayerPresence> usernameMatches = existingPlayers.entrySet().stream()
				.filter(entry -> entry.getValue().username().toLowerCase().contains(lowerQuery))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Second pass: matches by characterName, excluding already matched UUIDs
		Map<UUID, PlayerPresence> characterNameMatches = existingPlayers.entrySet().stream()
				.filter(entry -> !usernameMatches.containsKey(entry.getKey()))
				.filter(entry -> entry.getValue().characterName().toLowerCase().contains(lowerQuery))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Merge both results, username matches take priority
		Map<UUID, PlayerPresence> result = new LinkedHashMap<>();
		result.putAll(usernameMatches);
		result.putAll(characterNameMatches);

		return result;
	}

	public static SkinServerCache get(MinecraftServer server) {
		return ComponentManager.INSTANCE.getComponent(server, SkinServerCache.class);
	}


}
