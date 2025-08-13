package com.diamssword.characters.api;
import net.minecraft.nbt.NbtCompound;

public interface IPlayerComponent{
		public IPlayerAppearance getAppearence();
		public NbtCompound toNBT();
		public void fromNBT(NbtCompound nbt);

	}