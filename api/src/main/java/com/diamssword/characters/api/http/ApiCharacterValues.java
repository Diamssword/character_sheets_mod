package com.diamssword.characters.api.http;

import net.minecraft.nbt.NbtCompound;

public class ApiCharacterValues {
	public ApiSkinValues appearance;
	public ApiStatsValues stats;

	public NbtCompound toNBT() {
		var tag = new NbtCompound();
		tag.put("appearance", appearance.toNBT());
		tag.put("stats", stats.toNBT());
		return tag;
	}

	public ApiCharacterValues charactersfromNBT(NbtCompound tag) {
		appearance = new ApiSkinValues();
		appearance.fromNBT(tag.getCompound("appearance"));
		stats = new ApiStatsValues();
		stats.fromNBT(tag.getCompound("stats"));
		return this;
	}


}
