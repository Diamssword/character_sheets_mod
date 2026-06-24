package com.diamssword.characters.client;

import com.diamssword.characters.api.PlayerSkinInfos;
import com.diamssword.characters.api.http.SkinLayerValue;
import com.diamssword.characters.mixins.PlayerSkinProviderAccessor;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.SkinServerCache;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SkinsLoader {
	public static final SkinServerCache clientSkinCache = new SkinServerCache();
	private final Set<UUID> needReload = new HashSet<>();
	private final Set<UUID> requested = new HashSet<>();
	private final Set<UUID> requestedOfflineHead = new HashSet<>();
	private final File cacheDir;
	public static SkinsLoader instance = new SkinsLoader();

	public boolean doesNeedReload(UUID playerid) {
		return needReload.contains(playerid);
	}

	public boolean markReload(UUID playerid, boolean needed) {
		if (needed) {
			this.requested.remove(playerid);
			this.needReload.add(playerid);

		} else {
			if (needReload.contains(playerid)) {
				this.needReload.remove(playerid);
				return true;
			}
		}
		return false;
	}

	public SkinsLoader() {
		cacheDir = ((PlayerSkinProviderAccessor) MinecraftClient.getInstance().getSkinProvider()).getCacheDir();
	}

	public static void getHeadTexture(UUID playerID, Consumer<Identifier> callback) {

			instance.loadSkin(playerID, (a, b, c) -> callback.accept(b));
	}

	public void loadSkin(UUID userid, SkinTextureAvailableCallback callback) {
		var force = markReload(userid, false);
		Runnable runnable = () -> {
			MinecraftClient.getInstance().execute(() -> {
				RenderSystem.recordRenderCall(() -> {
					var skin = clientSkinCache.getSkin(userid);
					if (skin.isPresent()) {
						requested.remove(userid);
						var map1 = new HashMap<String, String>();
						map1.put("slim", Boolean.toString(skin.get().slim()));
						map1.put("displayname",skin.get().characterName());
						this.loadSkin(new LayerBasedMinecraftProfileTexture(skin.get().layers(), map1), callback, force);

					} else {
						if (!requested.contains(userid)) {
							Channels.MAIN.clientHandle().send(new SkinServerCache.RequestPlayerInfos(userid));
							requested.add(userid);
						}
					}
				});
			});
		};
		Util.getMainWorkerExecutor().execute(runnable);
	}

	public void loadSkin(GameProfile profile, SkinTextureAvailableCallback callback) {
		this.loadSkin(profile.getId(),callback);
	}

	private Identifier loadSkin(LayerBasedMinecraftProfileTexture profileTexture, @Nullable SkinTextureAvailableCallback callback, boolean force) {
		String string = Hashing.sha1().hashUnencodedChars(profileTexture.getHash()).toString();
		Identifier identifier = new Identifier("skins/" + string);
		AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getOrDefault(identifier, MissingSprite.getMissingSpriteTexture());
		if (force || abstractTexture == MissingSprite.getMissingSpriteTexture()) {
			File file = new File(cacheDir, string.length() > 2 ? string.substring(0, 2) : "xx");
			File file2 = new File(file, string);
			if (force && file2.exists())
				file2.delete();
			LayerBasedPlayerSkinTexture playerSkinTexture = new LayerBasedPlayerSkinTexture(file2, profileTexture.getData(), DefaultSkinHelper.getTexture(), () -> {
				if (callback != null) {
					callback.onSkinTextureAvailable(MinecraftProfileTexture.Type.SKIN, identifier, profileTexture);
				}

			});
			MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, playerSkinTexture);
		} else if (callback != null) {
			callback.onSkinTextureAvailable(MinecraftProfileTexture.Type.SKIN, identifier, profileTexture);
		}

		return identifier;
	}

	public static CompletableFuture<Map<UUID, PlayerSkinInfos>> requestPlayerProfiles(String query) {
		Channels.MAIN.clientHandle().send(new SkinServerCache.RequestPlayersMatching(query));
		var future = new CompletableFuture<Map<UUID, PlayerSkinInfos>>();
		ClientComesticsPacket.PlayerProfilesRequestCallback = future::complete;
		return future;
	}

	@Environment(EnvType.CLIENT)
	public interface SkinTextureAvailableCallback {
		void onSkinTextureAvailable(MinecraftProfileTexture.Type type, Identifier id, LayerBasedMinecraftProfileTexture texture);
	}



}

