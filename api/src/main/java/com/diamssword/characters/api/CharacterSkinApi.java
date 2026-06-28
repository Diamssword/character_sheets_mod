package com.diamssword.characters.api;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public interface CharacterSkinApi {
	public void getPlayerTexture(UUID playerID, Consumer<Identifier> callback);
	public void getPlayerHeadIconTexture(UUID playerID, Consumer<Identifier> callback);
	public CompletableFuture<Map<UUID, PlayerSkinInfos>> requestPlayerProfiles(String query);
}
