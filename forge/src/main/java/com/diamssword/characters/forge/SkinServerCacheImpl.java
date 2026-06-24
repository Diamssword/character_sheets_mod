package com.diamssword.characters.forge;

import com.diamssword.characters.api.PlayerSkinInfos;
import com.diamssword.characters.network.SkinServerCache;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class SkinServerCacheImpl extends SkinServerCache {
	public void readFromNbt(NbtCompound tag) {
		this.skinCache.clear();
		for (var k : tag.getKeys()) {
			try {
				var id = UUID.fromString(k);
				var t = tag.getCompound(k);
				var r = PlayerSkinInfos.fromNBT(t);
				skinCache.put(id, r);
			} catch (IllegalArgumentException ex) {

			}
		}
	}

	public NbtCompound toNBT( ) {
		NbtCompound tag=new NbtCompound();
		skinCache.forEach((k, v) -> {
			tag.put(k.toString(), v.toNBT());
		});
		return tag;
	}
}
