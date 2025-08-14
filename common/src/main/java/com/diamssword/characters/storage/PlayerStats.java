package com.diamssword.characters.storage;

import com.diamssword.characters.api.appearence.IPlayerAppearance;
import com.diamssword.characters.api.http.ApiCharacterValues;
import com.diamssword.characters.api.stats.IPlayerStats;
import com.diamssword.characters.storage.ClassesLoader;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats implements IPlayerStats {
	private final Map<String, Integer> stats = new HashMap<>();
	private final Map<String, Integer> storedXP = new HashMap<>();
	public final PlayerEntity player;

	public PlayerStats(PlayerEntity parent) {
		player = parent;
	}

	public void readFromNbt(NbtCompound nbt) {
		var st = nbt.getCompound("stats");
		var ls = ClassesLoader.getRoles();
		st.getKeys().forEach(k -> {
			if (ls.containsKey(k))
				stats.put(k, st.getInt(k));
		});
		var xps = nbt.getCompound("xp");
		xps.getKeys().forEach(k -> {
			try {
				if (ls.containsKey(k))
					storedXP.put(k, xps.getInt(k));
			} catch (IllegalArgumentException e) {
			}

		});
		if (!player.getWorld().isClient)
			onPlayerRespawn();
	}

	public NbtCompound writeToNbt() {
		var nbt = new NbtCompound();
		var nbt2 = new NbtCompound();
		var nbt1 = new NbtCompound();
		stats.forEach(nbt1::putInt);
		storedXP.forEach(nbt2::putInt);
		nbt.put("stats", nbt1);
		nbt.put("xp", nbt2);
		return nbt;
	}

	public int getLevel(String main) {
		return stats.getOrDefault(main, 0);
	}

	public int getPalier(String main) {
		var v = stats.getOrDefault(main, 0);
		var c = ClassesLoader.getRole(main);
		return c.map(statsRole -> statsRole.getPalierForLevel(v)).orElse(0);
	}

	public int getXp(String role) {
		return storedXP.getOrDefault(role, 0);
	}

	public void setLevel(String role, int count) {
		stats.put(role, count);
		ClassesLoader.onLevelChange(player, role, count);
	}

	public void setXp(String role, int count) {
		var l = getLevel(role);
		var nex = ClassesLoader.instance.getXpCostForLevel(l + 1);
		if (nex > 0 && count >= nex) {
			setLevel(role, l + 1);
			count = count - nex;
		}
		storedXP.put(role, count);
	}

	public int getOrCreate(String main, int level) {
		if (!stats.containsKey(main)) {
			stats.put(main, level);
		}
		return stats.get(main);
	}

	public void onPlayerRespawn() {
		for (var item : stats.entrySet()) {
			ClassesLoader.onLevelChange(player, item.getKey(), item.getValue());
		}
		player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(50);
	}

	@Override
	public void onCharacterLoad(PlayerEntity player, String newCharacterID, ApiCharacterValues newCharacter, @Nullable String oldCharacterID) {

	}
	public static NbtCompound serializer(IPlayerStats instance)
	{
		return instance.writeToNbt();
	}
	public static void unserializer(IPlayerStats instance,NbtCompound tag)
	{
		instance.readFromNbt(tag);
	}
}
