package com.diamssword.characters.api.http;

import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;

public class ApiSkinValues {
	public Map<String,String> additionals=new HashMap<>();
	public int size;
	public boolean slim;

	public NbtCompound toNBT() {
		var tag = new NbtCompound();
		var add=new NbtCompound();
		additionals.forEach(add::putString);
		tag.put("additionals",add);
		tag.putInt("size", size);
		tag.putBoolean("skinny", slim);
		return tag;
	}

	public ApiSkinValues fromNBT(NbtCompound tag) {
		additionals.clear();
		if(tag.contains("additionals"))
		{
			var a=tag.getCompound("additionals");
			a.getKeys().forEach(v->additionals.put(v,a.getString(v)));
		}
		size = Math.min(99, Math.max(50, tag.getInt("size")));
		slim = tag.getBoolean("skinny");
		return this;
	}

	/**
	 * @param tailleM  la partie Metre de la taille (en general 1M)
	 * @param tailleCM la partie centimetre de la taille (par defaut 80)
	 * @return le scale Y du joueur (entre 0 et 1 basé sur la taille standard d'1.8)
	 */
	public static float HeightMToMCScale(int tailleM, int tailleCM) {
		float v = tailleM + (tailleCM / 100f);
		return v / 1.80f;
	}
}
