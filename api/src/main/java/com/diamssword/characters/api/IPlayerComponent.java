package com.diamssword.characters.api;
import com.diamssword.characters.api.appearence.IPlayerAppearance;
import com.diamssword.characters.api.stats.IPlayerStats;
import net.minecraft.nbt.NbtCompound;

public interface IPlayerComponent{
		public IPlayerAppearance getAppearence();
	public IPlayerStats getStats();
		public NbtCompound toNBT();
		public void fromNBT(NbtCompound nbt);

	}