package com.diamssword.characters.storage;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.stats.StatsRole;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.DictionaryPackets;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class ClassesLoader implements SynchronousResourceReloader {
	private static final int MAX_LEVEL = 100;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Map<String, BiFunction<String, JsonObject, ? extends StatsRole>> roleFactories = new HashMap<>();

	private static final Logger LOGGER = LogUtils.getLogger();
	public static ClassesLoader instance = new ClassesLoader();
	private final Map<String, StatsRole> roles = new HashMap<>();
	private final Map<Integer, Integer> progression = new HashMap<>();
	private boolean shouldSync = false;
	public Identifier getId() {
		return new Identifier(getName());
	}
	@Override
	public String getName() {
		return Characters.MOD_ID+":classes";
	}

	public static Optional<StatsRole> getRole(String id) {
		return Optional.ofNullable(instance.roles.get(id));
	}

	public static Map<String, StatsRole> getRoles() {
		return instance.roles;
	}
	public static void registerRole(String id,BiFunction<String, JsonObject, ? extends StatsRole> factory)
	{
		roleFactories.put(id,factory);
	}
	public static void initEvents() {
		PlayerEvent.PLAYER_CLONE.register((oldPlayer, newPlayer, alive) -> {
			if (!alive) {
				if( ComponentManager.getPlayerDatas(newPlayer).getStats() instanceof PlayerStats ps)
					ps.onPlayerRespawn();
			}
		});

	}

	public static void onLevelChange(PlayerEntity pl, String role, int level) {
		getRole(role).ifPresent(r -> {
			r.onLevelChange(pl, level);
		});
	}

	public int getXpCostForLevel(int palier) {
		var res = 0;
		var last = 0;
		for (var p : progression.entrySet()) {
			if (palier < p.getKey() && p.getKey() > last) {
				last = p.getKey();
				res = p.getValue();
			}
		}
		return res;
	}

	public int missingXpForNext(PlayerEntity player, String competence) {
		var st = ComponentManager.getPlayerDatas(player).getStats();
		var xp = st.getXp(competence);
		var lev = st.getLevel(competence);
		if (lev >= MAX_LEVEL)
			return -1;
		var l = getXpCostForLevel(lev + 1);
		return l - xp;
	}

	public float percentOfXpForNext(PlayerEntity player, String competence) {
		var st = ComponentManager.getPlayerDatas(player).getStats();
		var xp = st.getXp(competence);
		var lev = st.getLevel(competence);
		if (lev >= MAX_LEVEL)
			return 1;
		var l = getXpCostForLevel(lev + 1);
		return (float) xp / l;
	}

	@Override
	public void reload(ResourceManager manager) {
		roles.clear();
		var id = Characters.asRessource("skills.json");
		var file = manager.getResource(id);
		if (file.isPresent()) {
			try {
				BufferedReader reader = file.get().getReader();
				try {
					readJson(JsonHelper.deserialize(GSON, reader, JsonObject.class));
				} finally {
					((Reader) reader).close();
				}
			} catch (JsonParseException | IOException | IllegalArgumentException exception) {
				LOGGER.error("Couldn't parse data file {} from {}", id, getId(), exception);
			}
		}
		shouldSync = true;
	}

	public void worldTick(MinecraftServer server) {
		if (shouldSync) {
			shouldSync = false;
			Channels.serverHandle(server).send(new DictionaryPackets.ClassesList(this));
		}
	}

	private void readJson(JsonObject jsonElement) {
		roles.clear();
		progression.clear();
		jsonElement.keySet().forEach(v -> {
			var ob = jsonElement.getAsJsonObject(v);
			if (v.equals("progression")) {
				ob.keySet().forEach(v1 -> {
					try {
						var key = Integer.parseInt(v1);
						var val = ob.get(v1).getAsInt();
						progression.put(key, val);
					} catch (NumberFormatException ignored) {
					}
				});
			} else if (!ob.has("disabled") || !ob.get("disabled").getAsBoolean()) {
				if (ob.has("name")) {
					var fac = roleFactories.get(v);
					if (fac == null)
						fac = (i, d) -> new StatsRole(i, d) {
							@Override
							public void init() {
							}
						};
					var r = fac.apply(v, ob);
					roles.put(v, r);
					r.init();
				} else
					LOGGER.error("Skill is missing name!");
			}
		});
	}

	public static void serializer(PacketByteBuf write, ClassesLoader val) {
		var obj = new JsonObject();
		var o1 = new JsonObject();
		val.progression.forEach((k, v) -> {
			o1.addProperty(k.toString(), v);
		});
		obj.add("progression", o1);
		val.roles.forEach((k, v) -> {
			var o = new JsonObject();
			o.addProperty("name", v.name);
			var tag = new NbtCompound();
			tag.putString("name", v.name);
			var arr = new JsonArray();
			for (var i : v.stages)
				arr.add(i);
			o.add("stages", arr);
			obj.add(k, o);
		});
		write.writeString(obj.toString());
	}

	public static ClassesLoader unserializer(PacketByteBuf read) {
		var res = new ClassesLoader();
		res.readJson(JsonHelper.deserialize(GSON, read.readString(), JsonObject.class));
		return res;
	}
}
