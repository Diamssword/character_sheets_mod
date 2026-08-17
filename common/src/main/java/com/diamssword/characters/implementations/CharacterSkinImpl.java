package com.diamssword.characters.implementations;

import com.diamssword.characters.api.CharacterSkinApi;
import com.diamssword.characters.api.IPlayerAppearanceProvider;
import com.diamssword.characters.api.PlayerSkinInfos;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.client.SkinsLoader;
import com.diamssword.characters.client.renders.ClothingLayer;
import com.diamssword.characters.client.renders.EntityClothingLayer;
import com.diamssword.characters.storage.ClothingLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
@Environment(EnvType.CLIENT)
public class CharacterSkinImpl implements CharacterSkinApi {
	@Override
	public void getPlayerTexture(UUID playerID, Consumer<Identifier> callback) {
		SkinsLoader.getSkinTexture(playerID,callback);
	}

	@Override
	public <T extends LivingEntity & IPlayerAppearanceProvider> void getEntityTexture(T entity, Consumer<Identifier> callback) {
		SkinsLoader.instance.loadSkin(entity,(a,b,c)->callback.accept(b));
	}

	@Override
	public void getPlayerHeadIconTexture(UUID playerID, Consumer<Identifier> callback) {
		SkinsLoader.getHeadTexture(playerID,callback);
	}
	@Override
	public CompletableFuture<Map<UUID, PlayerSkinInfos>> requestPlayerProfiles(String query) {
		return SkinsLoader.requestPlayerProfiles(query);
	}

	@Override
	public <T extends LivingEntity & IPlayerAppearanceProvider> FeatureRenderer<T, PlayerEntityModel<T>> getClothLayerRenderForEntity(FeatureRendererContext<T, PlayerEntityModel<T>> context, LayerDef layer, boolean altTexture) {
		return new EntityClothingLayer<>(context,layer,altTexture);
	}

	@Override
	public  <T extends LivingEntity & IPlayerAppearanceProvider> List<FeatureRenderer<T, PlayerEntityModel<T>>> getClothLayersForEntity(LivingEntityRenderer<T, PlayerEntityModel<T>> renderer) {
		List<FeatureRenderer<T, PlayerEntityModel<T>>> ls=new ArrayList<>();
		if(ClothingLoader.instance !=null) {
			for (LayerDef value : ClothingLoader.instance.getLayers().values()) {
				ls.add(getClothLayerRenderForEntity(renderer,value,false));
				if (value.getLayer2() > -1)
					ls.add(getClothLayerRenderForEntity(renderer,value,true));
			}
		}
		return ls;
	}
}
