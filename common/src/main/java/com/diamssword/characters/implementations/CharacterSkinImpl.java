package com.diamssword.characters.implementations;

import com.diamssword.characters.api.CharacterSkinApi;
import com.diamssword.characters.api.PlayerPresence;
import com.diamssword.characters.client.SkinsLoader;
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
public class CharacterSkinImpl implements CharacterSkinApi {
	@Override
	public void getHeadTexture(UUID playerID, Consumer<Identifier> callback) {
		SkinsLoader.getHeadTexture(playerID,callback);
	}

	@Override
	public CompletableFuture<Map<UUID, PlayerPresence>> requestPlayerProfiles(String query) {
		return SkinsLoader.requestPlayerProfiles(query);
	}
}
