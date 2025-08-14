package com.diamssword.characters.api.stats;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class StatsRole {
	public final String name;
	public final String id;
	public final int[] stages;

	public StatsRole(String id, JsonObject data) {
		this.id = id;
		int[] p = new int[0];
		if (data.has("stages")) {
			p = data.get("stages").getAsJsonArray().asList().stream().map(JsonElement::getAsInt).mapToInt(Integer::intValue).toArray();
		}
		this.name = data.get("name").getAsString();
		this.stages = p;
	}

	protected Map<Integer, StatsRolePalier> map = new TreeMap<>();
	protected Map<EntityAttribute, Function<Integer, EntityAttributeModifier>> globalModifiers = new HashMap<>();

	public abstract void init();


	public int create(int level, Consumer<StatsRolePalier> init) {
		var c = new StatsRolePalier(this, level);
		map.put(level, c);
		init.accept(c);
		return level;
	}

	public StatsRolePalier getPalierInfos(int palier) {
		return map.get(palier);
	}

	public int getPalierForLevel(int level) {
		var d = 0;
		for (int i = 0; i < stages.length; i++) {
			if (stages[i] <= level) {
				d = i + 1;
			} else
				return d;
		}
		return d;
	}

	public void addGlobalModifier(EntityAttribute attr, Function<Integer, EntityAttributeModifier> modifier) {
		this.globalModifiers.put(attr, modifier);
	}

	public Map<EntityAttribute, Function<Integer, EntityAttributeModifier>> getGlobalModifiers() {
		return globalModifiers;
	}

	public void onLevelChange(PlayerEntity pl, int level) {
		changeModifiers(pl, level);
	}

	private void changeModifiers(PlayerEntity pl, int level) {
		for (var set : map.entrySet()) {
			set.getValue().clearModifier(pl);
		}
		for (var set : globalModifiers.entrySet()) {
			var max = pl.getAttributeInstance(set.getKey());
			var r = set.getValue().apply(level);
			max.tryRemoveModifier(r.getId());
			max.addPersistentModifier(r);
		}
		var last = getPalierForLevel(level);
		while (last > 0) {
			var l = map.get(last);
			if (l == null)
				last--;
			else {
				l.changeModifiers(pl);
				break;
			}
		}


	}

}
