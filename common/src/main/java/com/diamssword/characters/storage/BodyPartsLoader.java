package com.diamssword.characters.storage;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.CharacterClothingApi;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.api.skin.BodyLayerCategory;
import com.diamssword.characters.api.skin.BodyLayerImageGroup;
import com.diamssword.characters.api.skin.BodyLayerInfo;
import com.diamssword.characters.api.skin.BodyLayerDictionary;
import com.diamssword.characters.client.CharactersClient;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.DictionaryPackets;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.Resource;
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

public class BodyPartsLoader implements SynchronousResourceReloader {

	public static BodyPartsLoader instance = new BodyPartsLoader();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, BodyLayerDictionary> skinParts = new HashMap<>();
	private final Map<String, BodyLayerInfo> layers = new HashMap<>();
	private boolean shouldSync = false;
	private String defaultDomain=Characters.config.serverOptions.defaultModIdentifierForTextures;
	private int skinResolution=64;
	public Identifier getId() {
		return new Identifier(getName());
	}

	public Optional<BodyLayerDictionary> getBodyParts(String layerID) {
		return Optional.ofNullable(skinParts.get(layerID));
	}
	public Map<String,BodyLayerInfo> getBodyLayers() {
		return layers;
	}
	public String getDefaultDomain() {
		return defaultDomain;
	}
	public int getSkinResolution() {
		return skinResolution;
	}

	@Override
	public String getName() {
		return Characters.MOD_ID+":body_parts";
	}

	private String getString(JsonObject object,String key,String or)
	{
		if(object.has(key))
			return object.get(key).getAsString();
		return or;
	}
	private float getFloat(JsonObject object,String key)
	{
		if(object.has(key))
			return object.get(key).getAsFloat();
		return 0f;
	}
	private boolean getBool(JsonObject object,String key)
	{
		if(object.has(key))
			return object.get(key).getAsBoolean();
		return false;
	}
	private BodyLayerImageGroup getImageGroup(JsonObject object)
	{
		if(object.has("id"))
		{
			if(object.has("subs"))
			{
				List<BodyLayerImageGroup> subs=new ArrayList<>();
				object.getAsJsonArray("subs").forEach((v1->{
					subs.add(getImageGroup(v1.getAsJsonObject()));
				}));
				return new BodyLayerImageGroup(object.get("id").getAsString(),subs.toArray(new BodyLayerImageGroup[0]));
			}
			return new BodyLayerImageGroup(object.get("id").getAsString(),new BodyLayerImageGroup[0]);
		}
		return null;
	}
	private void loadParts(JsonObject object)
	{
		object.keySet().forEach(key->{
			var ob=object.get(key).getAsJsonObject();
			if(ob.has("images"))
			{
				List<BodyLayerImageGroup> subs=new ArrayList<>();
				var arr=ob.getAsJsonArray("images");
				arr.forEach(v->{
					var g=getImageGroup(v.getAsJsonObject());
					if(g!=null && (g.hasSubs() || !g.id().equals("clear")) )
					{
						subs.add(g);
					}
				});
				skinParts.put(key,new BodyLayerDictionary(key,getString(ob,"title",key),new BodyLayerCategory[]{new BodyLayerCategory("default","default",subs.toArray(new BodyLayerImageGroup[0]))}));
			}
			else if(ob.has("cats"))
			{
				var cats=ob.getAsJsonObject("cats");
				List<BodyLayerCategory> subsC=new ArrayList<>();
				cats.keySet().forEach(key1->{
					var ob1=cats.getAsJsonObject(key1);
					if(ob1.has("images"))
					{
						List<BodyLayerImageGroup> subs=new ArrayList<>();
						var arr=ob1.getAsJsonArray("images");
						arr.forEach(v->{
							var g=getImageGroup(v.getAsJsonObject());
							if(g!=null && (g.hasSubs() || !g.id().equals("clear")) )
							{
								subs.add(g);
							}
						});
						subsC.add(new BodyLayerCategory(key1,getString(ob,"name",key1),subs.toArray(new BodyLayerImageGroup[0])));
					}
				});
				skinParts.put(key,new BodyLayerDictionary(key,getString(ob,"title",key),subsC.toArray(new BodyLayerCategory[0])));
			}
		});
	}
	private void loadLayers(JsonArray array)
	{
		array.forEach(v -> {
			var ob = v.getAsJsonObject();
			if (ob.has("name")) {
				var id = ob.get("name").getAsString();
				if(id.equals("base"))
				{
					if(ob.has("skinRes"))
						skinResolution=ob.get("skinRes").getAsInt();
				}
				if (!layers.containsKey(id)) {
						try {
							var layer=new BodyLayerInfo(id,getString(ob,"display",id),getFloat(ob,"size"),getBool(ob,"clearable"),getBool(ob,"multi"),getBool(ob,"splited"),getBool(ob,"external"));
							layers.put(id, layer);
						} catch (IllegalArgumentException e) {
							LOGGER.error("Layer definition with id: {} can't be parsed ", id);
						}

				} else
					LOGGER.error("Duplicate id for layer: {}", id);
			} else
				LOGGER.error("Layer is missing ID!");
		});
	}
	@Override
	public void reload(ResourceManager manager) {
		LOGGER.error("reload parts");
		skinParts.clear();
		layers.clear();
		var idL = new Identifier(this.getDefaultDomain(),"body_layers.json");
		var fileL = manager.getResource(idL);
		if(fileL.isEmpty())
			fileL = manager.getResource(Characters.asRessource("body_layers.jon"));
		if (fileL.isPresent()) {
			try {
				try(BufferedReader reader = fileL.get().getReader()) {
					JsonArray jsonElement = JsonHelper.deserialize(GSON, reader, JsonArray.class);
					loadLayers(jsonElement);
				} finally {
					shouldSync = true;
				}
			} catch (JsonParseException | IOException | IllegalArgumentException exception) {
				LOGGER.error("Couldn't parse data file {} from {}", idL, getName(), exception);
			}
		}
		var idL1 = new Identifier(this.getDefaultDomain(),"body_parts.json");

		var fileL1 = manager.getResource(idL1);
		if(fileL1.isEmpty())
			fileL1 = manager.getResource(Characters.asRessource("body_parts.jon"));
		if (fileL1.isPresent()) {
			try {
				try(BufferedReader reader = fileL1.get().getReader()) {
					JsonObject jsonElement = JsonHelper.deserialize(GSON, reader, JsonObject.class);
					loadParts(jsonElement);
				} finally {
					shouldSync = true;
				}
			} catch (JsonParseException | IOException | IllegalArgumentException exception) {
				LOGGER.error("Couldn't parse data file {} from {}", idL1, getName(), exception);
			}
		}
	}

	public void worldTick(MinecraftServer server) {
		if (shouldSync) {
			shouldSync = false;
			Channels.serverHandle(server).send(new DictionaryPackets.BodyPartList(this));
			if(server.isSingleplayer())
				CharactersClient.reloadPlayerRender();
		}
	}

	public static void serializer(PacketByteBuf write, BodyPartsLoader val) {
		NbtList lays = new NbtList();
		NbtList list = new NbtList();
		val.layers.forEach((u,v)-> lays.add(v.toNBT()));
		val.skinParts.forEach((u, v) -> list.add(v.toNBT()));

		var comp = new NbtCompound();
		comp.put("layers", lays);
		comp.put("list", list);
		comp.putInt("resolution",val.skinResolution);
		comp.putString("defaultDomain",Characters.config.serverOptions.defaultModIdentifierForTextures);
		write.writeNbt(comp);
	}

	public static BodyPartsLoader unserializer(PacketByteBuf read) {

		BodyPartsLoader loader = new BodyPartsLoader();
		var comp = read.readNbt();
		var list = comp.getList("list", NbtElement.COMPOUND_TYPE);
		var lays = comp.getList("layers", NbtElement.COMPOUND_TYPE);
		loader.skinResolution=comp.getInt("resolution");
		if(loader.skinResolution==0)
			loader.skinResolution=64;
		if(comp.contains("defaultDomain"))
			loader.defaultDomain=comp.getString("defaultDomain");
		lays.forEach(el->{
			var def=BodyLayerInfo.fromNBT((NbtCompound) el);
			if(def!=null)
				loader.layers.put(def.id(),def);
			else
				LOGGER.error("Couldn't parse packet data for layer: {}", el);
		});
		list.forEach(el -> {
			var def=BodyLayerDictionary.fromNBT((NbtCompound) el);
			if(def!=null)
				loader.skinParts.put(def.id(),def);
			else
				LOGGER.error("Couldn't parse packet data for body parts: {}", el);
		});
		return loader;
	}

}
