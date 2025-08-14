package com.diamssword.characters.api;

import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.api.stats.StatsRole;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class CharacterStatsApi {

	/**
	 * Register a factory for a role
	 * @param id the id of the role, the factory won't run if this id is not present in 'classes.json'
	 * @param factory the factory, most likely the constructor of your role if it implements StatsRole directly
	 */
	abstract public void registerRole(String id, BiFunction<String, JsonObject, ? extends StatsRole> factory);

	/**
	 *
	 * @param id the id of the role you want
	 * @return a StatsRole if it exists
	 */
	public abstract Optional<StatsRole> getRole(String id);

	/**
	 *
	 * @return get a map of all roles loaded with ther id as key
	 */
	public abstract Map<String, StatsRole> getRoles();

	/**
	 * get the total cost of xp to reach a certain level from the previous one
	 */
	public abstract int getXpCostForLevel(int level);

	/**
	 * get the missing xp required for the player to reach the next level
	 */
	public abstract int missingXpForNext(PlayerEntity player, String role);

	/**
	 * Return a float between 0 and 1 corresponding the percent of xp missing to reach next level
	 * @param player
	 * @param role
	 */
	public abstract float percentOfXpForNext(PlayerEntity player, String role);
}
