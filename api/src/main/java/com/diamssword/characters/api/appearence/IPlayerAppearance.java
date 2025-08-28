package com.diamssword.characters.api.appearence;

import com.diamssword.characters.api.ICharacterStored;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IPlayerAppearance extends ICharacterStored {


	/**
	 * Clone the data of a sourcePlayer, mostly used to render in-gui versions of the player model
	 * @param sourcePlayer
	 */
	public void clonePlayerAppearance(PlayerEntity sourcePlayer);

	/**
	 * unlock a piece of clothing
	 * @param id
	 */
	public void unlockCloth(Cloth id);

	/**
	 * lock a previously owned piece of clothing
	 * @param id
	 */
	public void lockCLoth(Cloth id);

	/**
	 * @return all owned cloths ids
	 */
	public ArrayList<Identifier> getUnlockedCloths();

	/**
	 * get current worn cloth for specified layer
	 * @param layer
	 * @return
	 */
	public Optional<Cloth> getEquippedCloth(LayerDef layer);

	/**
	 *
	 * @return cloths layers only
	 */
	public Map<String, Cloth> getEquippedCloths();

	/**
	 *
	 * @return all layers
	 */
	public Map<String,Cloth> getEquippedLayers();

	/**
	 * try to equip a cloth, it will fail if the player have not unlocked the cloth
	 * @param cloth the cloth to equip
	 * @return true if the cloth have been equipped, false otherwise
	 */
	public boolean equipCloth(Cloth cloth);
	/**
	 * force equip a cloth, even if the player haven't unlocked it
	 * @param layer the layer to equip to
	 * @param cloth the cloth to equip
	 */
	public void setCloth(String layer, @Nullable Cloth cloth);

	public void setCloth(Cloth cloth);
	public void removeCloth(String layer);
	/**
	 * Save the current cloths equipped to an 'outfit' slot that can be restored later
	 * @param name the displayname of the outfit
	 * @param index the slot index 0-6
	 */
	public void saveOutfit(String name, int index);

	/**
	 * get a pair composed of the name and index of all saved outfit
	 */
	public List<Pair<String,Integer>> getOutfits();

	/**
	 * Equip previously saved outfit
	 * @param index the slot index of the outfit
	 */
	public void equipOutfit(int index);

	/**
	 *
	 * return a float value scaling the base player height of ~1.8 blocks so a scale of 0.5 would make the player 0.9 blocks tall!
	 */
	public float getHeightScale();

	public void readFromNbt(NbtCompound tag);

	/**
	 *
	 * @param tag
	 * @param mode 0==write for server side storage, 1==write to send to all tracking client, 2== write to send to owner client only
	 * @return
	 */
	public NbtCompound writeToNbt(NbtCompound tag, int mode);
}
