package com.diamssword.characters.api;

import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.api.appearence.LayerDef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class CharacterClothingApi {
	abstract public void addCloth(Cloth cloth);
	abstract public void addLayer(LayerDef layer);
	abstract public List<String> getClothIds();
	abstract public Optional<Cloth> getCloth(String clothID);
	abstract public List<String> getCollections();
	abstract public List<Cloth> getClothsCollection(String collection);
	public List<LayerDef> getClothLayers() {
		return getLayers().values().stream().filter(v -> !v.isBodyPart()).toList();
	}
	abstract public Optional<LayerDef> getLayer(String id);
	abstract public Map<String, LayerDef> getLayers();
	abstract public List<Cloth> getAvailablesClothsCollectionForPlayer(PlayerEntity ent, String collection, LayerDef... layers);

	abstract public <T extends ICharacterStored> void attachComponentToCharacters(Identifier id, Function<ServerPlayerEntity,T> provider, Function<T,NbtCompound> serializer, BiConsumer<T,NbtCompound> unserializer);
	abstract public void unattachComponentFromCharacters(Identifier id);
}
