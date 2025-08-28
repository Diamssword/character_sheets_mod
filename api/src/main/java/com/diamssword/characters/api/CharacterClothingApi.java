package com.diamssword.characters.api;

import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.api.appearence.LayerDef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class CharacterClothingApi {
	abstract public void addCloth(Cloth cloth);
	abstract public void addLayer(LayerDef layer);
	abstract public List<Identifier> getClothIds();
	abstract public Optional<Cloth> getCloth(Identifier clothID);
	abstract public List<String> getCollections();
	abstract public List<Cloth> getClothsCollection(String collection);
	public List<LayerDef> getClothLayers() {
		return getLayers().values().stream().filter(v -> !v.isBodyPart()).toList();
	}
	abstract public Optional<LayerDef> getLayer(String id);
	abstract public Map<String, LayerDef> getLayers();
	abstract public List<Cloth> getAvailablesClothsCollectionForPlayer(PlayerEntity ent, String collection, LayerDef... layers);

	abstract public void clientAskEquipCloth(Identifier clothID, @Nullable String layerID);
	abstract public void  clientAskEquipOutfit(int index);
	abstract public void  clientAskSaveOutfit(String name,int index);
}
