package com.diamssword.characters.fabric;

import com.diamssword.characters.api.PlayerSkinInfos;
import com.diamssword.characters.network.SkinServerCache;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class SkinServerCacheImpl extends SkinServerCache implements Component {

	@Override
	public void readFromNbt(NbtCompound tag) {
		this.skinCache.clear();
		for (var k : tag.getKeys()) {
			try {
				var id = UUID.fromString(k);
				var t = tag.getCompound(k);
				var r =PlayerSkinInfos.fromNBT(t);
				skinCache.put(id, r);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		skinCache.forEach((k, v) -> {
			tag.put(k.toString(), v.toNBT());
		});
	}
}
