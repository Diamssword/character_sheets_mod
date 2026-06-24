package com.diamssword.characters.api;

import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.api.skin.BodyLayerDictionary;
import com.diamssword.characters.api.skin.BodyLayerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BodyPartsApi {
	public Optional<BodyLayerDictionary> getBodyParts(String layerID);

	public Map<String, BodyLayerInfo> getBodyLayers();

	public Optional<BodyLayerInfo> getBodyLayer(String layerID);

	public int getSkinResolution();

	public String getDefaultNameSpace();
}
