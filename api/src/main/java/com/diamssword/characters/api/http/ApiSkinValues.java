package com.diamssword.characters.api.http;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.HashMap;
import java.util.Map;

public class ApiSkinValues {

	public SkinLayerValue[] layers=new SkinLayerValue[0];
	public int size;
	public boolean slim;

	public NbtCompound toNBT() {
		var tag = new NbtCompound();
		var add=new NbtList();
		for(SkinLayerValue layer : layers) {
			add.add(layer.toNBT());
		}
		tag.put("layers",add);
		tag.putInt("size", size);
		tag.putBoolean("skinny", slim);
		return tag;
	}

	public ApiSkinValues fromNBT(NbtCompound tag) {

		if(tag.contains("layers"))
		{
			var a=tag.getList("layers", NbtElement.COMPOUND_TYPE);
			layers=new SkinLayerValue[a.size()];
			for(int i = 0; i < a.size(); i++) {
				layers[i]=new SkinLayerValue().fromNBT(a.getCompound(i));
			}
		}
		else
			layers=new SkinLayerValue[0];
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
