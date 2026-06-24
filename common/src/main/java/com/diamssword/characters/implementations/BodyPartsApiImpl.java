package com.diamssword.characters.implementations;

import com.diamssword.characters.api.BodyPartsApi;
import com.diamssword.characters.api.CharacterClothingApi;
import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.api.skin.BodyLayerDictionary;
import com.diamssword.characters.api.skin.BodyLayerInfo;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.storage.BodyPartsLoader;
import com.diamssword.characters.storage.ClothingLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BodyPartsApiImpl implements BodyPartsApi {

	@Override
	public Optional<BodyLayerDictionary> getBodyParts(String layerID) {
		return BodyPartsLoader.instance.getBodyParts(layerID);
	}

	@Override
	public Map<String, BodyLayerInfo> getBodyLayers() {
		return BodyPartsLoader.instance.getBodyLayers();
	}

	@Override
	public Optional<BodyLayerInfo> getBodyLayer(String layerID) {
		return Optional.ofNullable(BodyPartsLoader.instance.getBodyLayers().get(layerID));
	}

	@Override
	public int getSkinResolution() {
		return BodyPartsLoader.instance.getSkinResolution();
	}

	@Override
	public String getDefaultNameSpace() {
		return BodyPartsLoader.instance.getDefaultDomain();
	}
}
