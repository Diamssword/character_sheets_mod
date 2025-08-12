package com.diamssword.characters;

import com.diamssword.characters.api.Cloth;
import com.diamssword.characters.api.LayerDef;
import com.diamssword.characters.client.CharactersClient;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.DictionaryPackets;
import com.diamssword.characters.storage.ComponentManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class ClothingLoader implements SynchronousResourceReloader {

	public static ClothingLoader instance = new ClothingLoader();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Logger LOGGER = LogUtils.getLogger();
	private Map<String, Cloth> cloths = new HashMap<>();
	private Map<String, LayerDef> layers = new HashMap<>();
	private final List<String> collections = new ArrayList<>();
	private boolean shouldSync = false;

	public ClothingLoader() {
	}
	public Identifier getId() {
		return new Identifier(getName());
	}
	public Optional<Cloth> getCloth(String id) {
		return Optional.ofNullable(cloths.get(id));
	}

	public List<String> getCollections() {
		return new ArrayList<>(collections);
	}

	public List<Cloth> getClothsCollection(String collection) {
		return cloths.values().stream().filter(v -> collection.equals("all") || v.collection().equals(collection)).toList();
	}

	public List<Cloth> getClothsCollection(String collection, LayerDef... layers) {
		var lays = Arrays.stream(layers).map(LayerDef::getId).toList();
		return cloths.values().stream().filter(v -> (v.collection().equals(collection) || collection.equals("all")) && lays.contains(v.layer().getId())).toList();
	}
	public List<String> getClothIds()
	{
		return cloths.keySet().stream().toList();
	}
	public List<LayerDef> getClothLayers() {
		return layers.values().stream().filter(v -> !v.isBodyPart()).toList();
	}
	public Optional<LayerDef> getLayer(String id) {
		return Optional.ofNullable(layers.get(id));
	}
	public Map<String,LayerDef> getLayers() {
		return layers;
	}
	public List<Cloth> getAvailablesClothsCollectionForPlayer(PlayerEntity ent, String collection, LayerDef... layers) {

		var lays = Arrays.stream(layers).map(LayerDef::getId).toList();
		if (ent.isCreative())
			return cloths.values().stream().filter(v -> (collection.equals("all") || v.collection().equals(collection)) && lays.contains(v.layer().getId())).toList();
		else {
			var unl = ComponentManager.getPlayerDatas(ent).getAppearence().getUnlockedCloths();
			return cloths.values().stream().filter(v -> (collection.equals("all") || v.collection().equals(collection)) && lays.contains(v.layer().getId()) && unl.contains(v.layer().getId()+"_"+v.id())).toList();
		}
	}

	public List<Cloth> getForLayers(LayerDef... layers) {	List<Cloth> res = new ArrayList<>();
		var ls = Arrays.stream(layers).map(LayerDef::getId).toList();
		cloths.forEach((k, v) -> {
			if (ls.contains(v.layer().getId())) {
				res.add(v);
			}
		});
		return res;
	}

	@Override
	public String getName() {
		return Characters.MOD_ID+":cloths";
	}
	private void loadCLoths(JsonArray array)
	{
		array.forEach(v -> {
			var ob = v.getAsJsonObject();
			if (ob.has("id")) {
				var id1 = ob.get("layer").getAsString() + "_" + ob.get("id").getAsString();
				if (!cloths.containsKey(id1)) {
					if (ob.has("layer") && ob.has("name")) {
							String col = "default";
							if (ob.has("collection"))
								col = ob.get("collection").getAsString();
							var lay=layers.get(ob.get("layer").getAsString());
							if(lay !=null) {
								Cloth table = new Cloth(ob.get("id").getAsString(), ob.get("name").getAsString(),lay, col);
								if (!collections.contains(col))
									collections.add(col);
								cloths.put(id1, table);
							}
							else
								LOGGER.error("Layer for clothing with id: {} can't be parsed (layer {})", id1, ob.get("layer"));

					} else
						LOGGER.error("Missing name of layer for clothing with id: {}", id1);
				} else
					LOGGER.error("Duplicate id for clothing: {}", id1);
			} else
				LOGGER.error("Clothing is missing ID!");
		});
	}
	private void loadLayers(JsonArray array)
	{
		array.forEach(v -> {
			var ob = v.getAsJsonObject();
			if (ob.has("id")) {
				var id = ob.get("id").getAsString();
				if (!layers.containsKey(id)) {
					if (ob.has("layer1")) {
						try {
							var layer=new LayerDef(id,ob.get("layer1").getAsInt());
							if(ob.has("layer1"))
								layer.setLayer2(ob.get("layer1").getAsInt());
							if(ob.has("forced"))
								layer.setForced(ob.get("forced").getAsBoolean());
							if(ob.has("baseLayer"))
								layer.setBaseLayer(ob.get("baseLayer").getAsBoolean());
							if(ob.has("bodyPart"))
								layer.setBodyPart(ob.get("bodyPart").getAsBoolean());
							if(ob.has("displayMode"))
								layer.setDisplayMode(ob.get("displayMode").getAsInt());
							layers.put(id, layer);
						} catch (IllegalArgumentException e) {
							LOGGER.error("Layer definition with id: {} can't be parsed ", id);
						}
					} else
						LOGGER.error("Layer {} is missing a layer1 field", id);
				} else
					LOGGER.error("Duplicate id for layer: {}", id);
			} else
				LOGGER.error("Layer is missing ID!");
		});
	}
	@Override
	public void reload(ResourceManager manager) {
		cloths = new HashMap<>();
		collections.clear();
		layers.clear();
		var idL = Characters.asRessource("layers.json");
		var fileL = manager.getResource(idL);
		if (fileL.isPresent()) {
			try {
				BufferedReader reader = fileL.get().getReader();
				try {
					JsonArray jsonElement = JsonHelper.deserialize(GSON, reader, JsonArray.class);
					loadLayers(jsonElement);
				} finally {
					((Reader) reader).close();
					shouldSync = true;
				}
			} catch (JsonParseException | IOException | IllegalArgumentException exception) {
				LOGGER.error("Couldn't parse data file {} from {}", idL, getName(), exception);
			}
		}
		var id = Characters.asRessource("cloths.json");
		var file = manager.getResource(id);
		if (file.isPresent()) {
			try {
				BufferedReader reader = file.get().getReader();
				try {
					JsonArray jsonElement = JsonHelper.deserialize(GSON, reader, JsonArray.class);
					loadCLoths(jsonElement);
				} finally {
					((Reader) reader).close();
					shouldSync = true;
				}
			} catch (JsonParseException | IOException | IllegalArgumentException exception) {
				LOGGER.error("Couldn't parse data file {} from {}", id, getName(), exception);
			}
		}
	}

	public void worldTick(MinecraftServer server) {
		if (shouldSync) {
			shouldSync = false;
			Channels.serverHandle(server).send(new DictionaryPackets.ClothingList(this));
			if(server.isSingleplayer())
				CharactersClient.reloadPlayerRender();
		}
	}

	public static void serializer(PacketByteBuf write, ClothingLoader val) {
		NbtList lays = new NbtList();
		NbtList list = new NbtList();
		NbtList collection = new NbtList();
		val.layers.forEach((u,v)-> lays.add(v.toNBT()));
		val.cloths.forEach((u, v) -> {
			var v1 = v.toNBT();
			v1.putString("id", u);
			v1.putString("texture", v.id());
			list.add(v1);
		});
		val.collections.forEach(c -> {
			var v1 = new NbtCompound();
			v1.putString("id", c);
			collection.add(v1);
		});
		var comp = new NbtCompound();
		comp.put("layers", lays);
		comp.put("list", list);
		comp.put("collection", collection);
		write.writeNbt(comp);
	}

	public static ClothingLoader unserializer(PacketByteBuf read) {

		ClothingLoader loader = new ClothingLoader();
		var comp = read.readNbt();
		var list = comp.getList("list", NbtElement.COMPOUND_TYPE);
		var list1 = comp.getList("collection", NbtElement.COMPOUND_TYPE);
		var lays = comp.getList("layers", NbtElement.COMPOUND_TYPE);
		list1.forEach(c -> {
			loader.collections.add(((NbtCompound) c).getString("id"));
		});
		lays.forEach(el->{
			var def=LayerDef.fromNBT((NbtCompound) el);
			if(def!=null)
				loader.layers.put(def.getId(),def);
			else
				LOGGER.error("Couldn't parse packet data for layer: {}", el);
		});
		list.forEach(el -> {
			try {
				var lay=loader.layers.get(((NbtCompound) el).getString("layer"));
				if(lay !=null) {
					var t = Cloth.fromNBT((NbtCompound) el, ((NbtCompound) el).getString("texture"), lay);
					if (t != null)
						loader.cloths.put(((NbtCompound) el).getString("id"), t);
				}
			} catch (Exception e) {
				LOGGER.error("Couldn't parse packet data for cloth: {}", el, e);
			}
		});
		return loader;
	}

}
