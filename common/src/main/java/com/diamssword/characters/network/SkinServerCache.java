package com.diamssword.characters.network;

import com.diamssword.characters.Utils;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.PlayerSkinInfos;
import com.diamssword.characters.api.http.SkinLayerValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.stream.Collectors;

public class SkinServerCache {

	protected final Map<UUID, PlayerSkinInfos> skinCache = new HashMap<>();

	public record SendPlayerInfos(UUID player,PlayerSkinInfos infos) {
	}
	public record SendPlayerMatchInfos(Map<UUID,PlayerSkinInfos> players) {
	}
	public record RequestPlayerInfos(UUID player) {
	}


	public record RequestPlayersMatching(String query) {
	}

	public SkinServerCache() {

	}
	public void clearCache()
	{
		skinCache.clear();
	}
	public Optional<PlayerSkinInfos> getSkin(UUID user) {
		return Optional.ofNullable(skinCache.get(user));
	}


	public void removeFromCache(UUID uuid) {
		skinCache.remove(uuid);
	}
	public static void init() {
		Channels.MAIN.registerClientboundDeferred(SendPlayerInfos.class);
		Channels.MAIN.registerServerbound(RequestPlayersMatching.class, (msg, ctx) -> {
			Channels.MAIN.serverHandle(ctx.player()).send(new SendPlayerMatchInfos(SkinServerCache.get(ctx.player().server).getPlayersMatching(msg.query)));

		});
		Channels.MAIN.registerServerbound(RequestPlayerInfos.class, (msg, ctx) -> {
			var skin = SkinServerCache.get(ctx.player().server).skinCache.get(msg.player);
			if (skin != null)
				Channels.MAIN.serverHandle(ctx.player()).send(new SendPlayerInfos(msg.player, skin));
		});
	}
	public void setActiveCharacter(UUID playerID,String username, String characterName, SkinLayerValue[] layers,boolean slim) {

		skinCache.put(playerID, new PlayerSkinInfos(characterName,username,layers,slim ));
	}

	public void setActiveCharacter(PlayerEntity player, String characterName, SkinLayerValue[] layers,boolean slim) {
		skinCache.put(player.getUuid(), new PlayerSkinInfos(characterName, player.getGameProfile().getName(),layers,slim ));
	}

	public Map<UUID, PlayerSkinInfos> getPlayersMatching(String query) {
		String lowerQuery = query.toLowerCase();

		// First pass: matches by username
		Map<UUID, PlayerSkinInfos> usernameMatches = skinCache.entrySet().stream()
				.filter(entry -> entry.getValue().username().toLowerCase().contains(lowerQuery))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Second pass: matches by characterName, excluding already matched UUIDs
		Map<UUID, PlayerSkinInfos> characterNameMatches = skinCache.entrySet().stream()
				.filter(entry -> !usernameMatches.containsKey(entry.getKey()))
				.filter(entry -> entry.getValue().characterName().toLowerCase().contains(lowerQuery))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Merge both results, username matches take priority
		Map<UUID, PlayerSkinInfos> result = new LinkedHashMap<>();
		result.putAll(usernameMatches);
		result.putAll(characterNameMatches);

		return result;
	}

	public static SkinServerCache get(MinecraftServer server) {
		return ComponentManager.INSTANCE.getComponent(server, SkinServerCache.class);
	}


}
