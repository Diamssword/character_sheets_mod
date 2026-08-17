package com.diamssword.characters.api;

import com.diamssword.characters.api.appearence.LayerDef;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public interface CharacterSkinApi {
	public void getPlayerTexture(UUID playerID, Consumer<Identifier> callback);
	public <T extends LivingEntity & IPlayerAppearanceProvider > void getEntityTexture(T entity, Consumer<Identifier> callback);
	public void getPlayerHeadIconTexture(UUID playerID, Consumer<Identifier> callback);
	public CompletableFuture<Map<UUID, PlayerSkinInfos>> requestPlayerProfiles(String query);

	public <T extends LivingEntity & IPlayerAppearanceProvider > FeatureRenderer<T, PlayerEntityModel<T>> getClothLayerRenderForEntity(FeatureRendererContext<T, PlayerEntityModel<T>> context, LayerDef layer, boolean altTexture);
	public <T extends LivingEntity & IPlayerAppearanceProvider > List<FeatureRenderer<T, PlayerEntityModel<T>>> getClothLayersForEntity(LivingEntityRenderer<T, PlayerEntityModel<T>> renderer);
}
