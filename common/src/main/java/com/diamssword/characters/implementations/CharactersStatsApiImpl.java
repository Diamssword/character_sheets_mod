package com.diamssword.characters.implementations;

import com.diamssword.characters.api.CharacterStatsApi;
import com.diamssword.characters.api.stats.StatsRole;
import com.diamssword.characters.storage.ClassesLoader;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class CharactersStatsApiImpl extends CharacterStatsApi {


	@Override
	public void registerRole(String id, BiFunction<String, JsonObject, ? extends StatsRole> factory) {
		ClassesLoader.registerRole(id,factory);
	}

	@Override
	public Optional<StatsRole> getRole(String id) {
		return ClassesLoader.getRole(id);
	}

	@Override
	public Map<String, StatsRole> getRoles() {
		return ClassesLoader.getRoles();
	}

	@Override
	public int getXpCostForLevel(int palier) {
		return ClassesLoader.instance.getXpCostForLevel(palier);
	}

	@Override
	public int missingXpForNext(PlayerEntity player, String competence) {
		return ClassesLoader.instance.missingXpForNext(player,competence);
	}

	@Override
	public float percentOfXpForNext(PlayerEntity player, String competence) {
		return ClassesLoader.instance.percentOfXpForNext(player,competence);
	}
}
