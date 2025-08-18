package com.diamssword.characters.client;

import com.diamssword.characters.api.PlayerPresence;
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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SkinsLoader {
	public static final SkinServerCache clientSkinCache = new SkinServerCache();
	private final Set<UUID> needReload = new HashSet<>();
	private final Set<UUID> requested = new HashSet<>();
	private final Set<UUID> requestedOfflineHead = new HashSet<>();
	private final Map<UUID, Set<Consumer<Identifier>>> pendingOfflineheadRequest = new HashMap<>();
	private final Map<UUID, Identifier> offlineHeadCache = new HashMap<>();
	private final File cacheDir;
	public static SkinsLoader instance = new SkinsLoader();

	public boolean doesNeedReload(UUID playerid) {
		return needReload.contains(playerid);
	}

	public boolean markReload(UUID playerid, boolean needed) {
		if (needed) {
			this.requested.remove(playerid);
			this.offlineHeadCache.remove(playerid);
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
		var profile = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(playerID);
		if (profile != null) {
			instance.loadSkin(profile.getProfile().getId(), true, (a, b, c) -> callback.accept(b));
			return;
		}
		instance.getOfflineHead(playerID, callback);
	}

	public void loadSkinHeadOffline(UUID player, String base64) {
		Runnable runnable = () -> {
			MinecraftClient.getInstance().execute(() -> {
				RenderSystem.recordRenderCall(() -> {
					var txt = new B64MinecraftProfileTexture(base64, new HashMap<>());
					String string = Hashing.sha1().hashUnencodedChars(txt.getHash()).toString();
					Identifier identifier = new Identifier("heads/" + string);
					B64PlayerSkinTexture playerSkinTexture = new B64PlayerSkinTexture(null, base64, DefaultSkinHelper.getTexture(), () -> {
						offlineHeadCache.put(player, identifier);
						var k = pendingOfflineheadRequest.get(player);
						if (k != null) {
							k.forEach(x -> x.accept(identifier));
							pendingOfflineheadRequest.remove(player);
						}
					});
					MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, playerSkinTexture);
				});
			});
		};
		Util.getMainWorkerExecutor().execute(runnable);
	}

	public void loadSkin(UUID userid, boolean isHead, SkinTextureAvailableCallback callback) {
		var force = markReload(userid, false);
		Runnable runnable = () -> {
			MinecraftClient.getInstance().execute(() -> {
				RenderSystem.recordRenderCall(() -> {
					var skin = clientSkinCache.getSkin(userid);
					if (skin.isPresent()) {
						requested.remove(userid);
						var map1 = new HashMap<String, String>();
						map1.put("slim", skin.get().slim() ? "true" : "false");
						this.loadSkin(new B64MinecraftProfileTexture(isHead ? skin.get().head() : skin.get().skin(), map1), callback, force, isHead ? "heads" : "skins");

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

	private void getOfflineHead(UUID userid, Consumer<Identifier> callback) {
		Runnable runnable = () -> {
			MinecraftClient.getInstance().execute(() -> {
				RenderSystem.recordRenderCall(() -> {
					var skin = offlineHeadCache.get(userid);
					if (skin != null) {
						requestedOfflineHead.remove(userid);
						callback.accept(offlineHeadCache.get(userid));
					} else {
						if (!requestedOfflineHead.contains(userid)) {
							Channels.MAIN.clientHandle().send(new SkinServerCache.RequestPlayerPresence(userid));
							if (!pendingOfflineheadRequest.containsKey(userid))
								pendingOfflineheadRequest.put(userid, new HashSet<>());
							pendingOfflineheadRequest.get(userid).add(callback);
							requestedOfflineHead.add(userid);
						}
					}
				});
			});
		};
		Util.getMainWorkerExecutor().execute(runnable);
	}

	public void loadSkin(GameProfile profile, SkinTextureAvailableCallback callback) {
		this.loadSkin(profile.getId(), false, callback);
	}

	private Identifier loadSkin(B64MinecraftProfileTexture profileTexture, @Nullable SkinTextureAvailableCallback callback, boolean force, String type) {

		String string = Hashing.sha1().hashUnencodedChars(profileTexture.getHash()).toString();
		Identifier identifier = new Identifier(type + "/" + string);
		AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getOrDefault(identifier, MissingSprite.getMissingSpriteTexture());
		if (force || abstractTexture == MissingSprite.getMissingSpriteTexture()) {
			File file = new File(cacheDir, string.length() > 2 ? string.substring(0, 2) : "xx");
			File file2 = new File(file, string);
			if (force && file2.exists())
				file2.delete();
			B64PlayerSkinTexture playerSkinTexture = new B64PlayerSkinTexture(file2, profileTexture.getData(), DefaultSkinHelper.getTexture(), () -> {
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

	public static CompletableFuture<Map<UUID, PlayerPresence>> requestPlayerProfiles(String query) {
		Channels.MAIN.clientHandle().send(new SkinServerCache.RequestPlayersMatching(query));
		var future = new CompletableFuture<Map<UUID, PlayerPresence>>();
		ClientComesticsPacket.PlayerProfilesRequestCallback = future::complete;
		return future;
	}

	@Environment(EnvType.CLIENT)
	public interface SkinTextureAvailableCallback {
		void onSkinTextureAvailable(MinecraftProfileTexture.Type type, Identifier id, B64MinecraftProfileTexture texture);
	}



}

