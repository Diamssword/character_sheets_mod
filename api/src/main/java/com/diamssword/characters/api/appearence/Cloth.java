package com.diamssword.characters.api.appearence;

import net.minecraft.nbt.NbtCompound;

public record Cloth(String id, String name, LayerDef layer, String collection) {
		public NbtCompound toNBT() {
			var res = new NbtCompound();
			res.putString("name", name);
			res.putString("layer", layer.getId());
			res.putString("collection", collection);
			return res;
		}

		public static Cloth fromNBT(NbtCompound comp, String id,LayerDef layer) {
			try {

				return new Cloth(id, comp.getString("name"), layer, comp.getString("collection"));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}