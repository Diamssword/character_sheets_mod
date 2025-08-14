package com.diamssword.characters.api.stats;

import com.diamssword.characters.api.ICharacterStored;
import net.minecraft.nbt.NbtCompound;

public interface IPlayerStats extends ICharacterStored {
	public void readFromNbt(NbtCompound nbt);
	public NbtCompound writeToNbt();

	/**
	 * Get the current level for a role
	 * @param role
	 * @return
	 */
	public int getLevel(String role);

	/**
	 * get the current palier for a role
	 * a palier is a certain level defined in the json file, it will always return the palier lower or equal to the current level
	 * @param role
	 * @return
	 */
	public int getPalier(String role);
	public int getXp(String role);

	/**
	 * set the level for a role
	 * @param role
	 * @param level
	 */
	public void setLevel(String role, int level);

	/**
	 * set the amount of xp for a role
	 * @param role
	 * @param amount
	 */
	public void setXp(String role, int amount);

	/**
	 * Get the level of a role, if there is none, create this role with the defaultValue as level and return it
	 * @param role
	 * @param defaultValue
	 * @return the level of the role
	 */
	int getOrCreate(String role, int defaultValue);
}
