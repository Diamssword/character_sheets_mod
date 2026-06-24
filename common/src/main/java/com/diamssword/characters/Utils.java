package com.diamssword.characters;

import com.diamssword.characters.api.PlayerSkinInfos;
import com.diamssword.characters.network.SkinServerCache;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class Utils {

	public static Function<MinecraftServer,SkinServerCache> skinServerCacheSupplier;
	public static UUID parseUUID(String uuid) {
		if (uuid.contains("-"))
			return UUID.fromString(uuid);
		return UUID.fromString(uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
	}

	public static String UUIDToString(UUID uuid) {
		return uuid.toString().replaceAll("-", "");
	}

	public static Optional<PlayerSkinInfos> getSkinServerCacheSideSafe(PlayerEntity player)
	{
		return skinServerCacheSupplier.apply(player.getServer()).getSkin(player.getUuid());
	}
}
