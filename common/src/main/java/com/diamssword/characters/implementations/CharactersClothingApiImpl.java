package com.diamssword.characters.implementations;

import com.diamssword.characters.api.CharacterClothingApi;
import com.diamssword.characters.api.ICharacterStored;
import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.storage.ClothingLoader;
import com.diamssword.characters.storage.PlayerCharacters;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CharactersClothingApiImpl extends CharacterClothingApi {

	@Override
	public void addCloth(Cloth cloth) {
		ClothingLoader.instance.addCloth(cloth);
	}

	@Override
	public void addLayer(LayerDef layer) {
		ClothingLoader.instance.addLayer(layer);
	}

	@Override
	public List<String> getClothIds() {
		return ClothingLoader.instance.getClothIds();
	}

	@Override
	public Optional<Cloth> getCloth(String clothID) {
		return ClothingLoader.instance.getCloth(clothID);
	}

	@Override
	public List<String> getCollections() {
		return ClothingLoader.instance.getCollections();
	}

	@Override
	public List<Cloth> getClothsCollection(String collection) {
		return ClothingLoader.instance.getClothsCollection(collection);
	}

	@Override
	public Optional<LayerDef> getLayer(String id) {
		return ClothingLoader.instance.getLayer(id);
	}

	@Override
	public Map<String, LayerDef> getLayers() {
		return ClothingLoader.instance.getLayers();
	}

	@Override
	public List<Cloth> getAvailablesClothsCollectionForPlayer(PlayerEntity ent, String collection, LayerDef... layers) {
		return ClothingLoader.instance.getAvailablesClothsCollectionForPlayer(ent, collection,layers);
	}
	@Override
	public void clientAskEquipCloth(String clothID, @Nullable String layerID) {
		Channels.MAIN.clientHandle().send(new CosmeticsPackets.EquipCloth(clothID,layerID));
	}

	@Override
	public void clientAskEquipOutfit(int index) {
		Channels.MAIN.clientHandle().send(new CosmeticsPackets.EquipOutfit(index));
	}

	@Override
	public void clientAskSaveOutfit(String name, int index) {
		Channels.MAIN.clientHandle().send(new CosmeticsPackets.SaveOutfit(name,index));
	}
}
