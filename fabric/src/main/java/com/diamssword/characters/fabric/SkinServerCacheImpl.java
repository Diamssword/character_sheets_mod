package com.diamssword.characters.fabric;

import com.diamssword.characters.network.SkinServerCache;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.WorldProperties;

import java.util.UUID;

public class SkinServerCacheImpl extends SkinServerCache implements Component {

	@Override
	public void readFromNbt(NbtCompound tag) {
		existingPlayers.clear();
		for (var k : tag.getKeys()) {
			try {
				var id = UUID.fromString(k);
				var t = tag.getCompound(k);
				var r = new PlayerPresence(tag.getString("name"), t.getString("username"), t.getString("head"));
				existingPlayers.put(id, r);
			} catch (IllegalArgumentException ex) {

			}
		}
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		existingPlayers.forEach((k, v) -> {
			var t1 = new NbtCompound();
			t1.putString("name", v.characterName());
			t1.putString("username", v.username());
			t1.putString("head", v.head());
			tag.put(k.toString(), t1);
		});
	}
}
