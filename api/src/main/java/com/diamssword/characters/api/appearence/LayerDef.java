package com.diamssword.characters.api.appearence;

import net.minecraft.nbt.NbtCompound;

public class LayerDef {

	public final String id;
	private int layer1;
	private int layer2=-1;
	private boolean forced=false;
	private boolean baseLayer=false;
	private boolean bodyPart=false;
	private int displayMode=0;
	public LayerDef(String id,int layer1)
	{
		this.id=id;
		this.layer1=layer1;
	}

	public String getId() {
		return id;
	}
	/**
	 * The height of the base layer, the lowest the number, the closest to the skin
	 */
	public int getLayer1() {
		return layer1;
	}

	/**
	 * The height of the base layer, the lowest the number, the closest to the skin
	 */
	public LayerDef setLayer1(int layer1) {
		this.layer1 = layer1;
		return this;
	}
	/**
	 * The height of the 'headwear' layer, the lowest the number, the closest to the skin, this layer is optional and won't render if < 0
	 */
	public int getLayer2() {
		return layer2;
	}
	/**
	 * The height of the 'headwear' layer, the lowest the number, the closest to the skin, this layer is optional and won't render if < 0
	 */
	public LayerDef setLayer2(int layer2) {
		this.layer2 = layer2;
		return this;
	}

	/**
	 * if true, the player will always be forced to have something equipped in this layer
	 */
	public boolean isForced() {
		return forced;
	}

	/**
	 * Tell the mod to always force player to have something equipped on this layer
	 */
	public LayerDef setForced(boolean forced) {
		this.forced = forced;
		return this;
	}


	/**
	 * If true, the skin additional layers datas pulled from the online character creator will be allowed to edit this layer (think hairs for exemple)
	 */
	public boolean isBaseLayer() {
		return baseLayer;
	}
	/**
	 * If true, the skin additional layers datas pulled from the online character creator will be allowed to edit this layer (think hairs for exemple)
	 */
	public LayerDef setBaseLayer(boolean baseLayer) {
		this.baseLayer = baseLayer;
		return this;
	}

	public boolean isBodyPart() {
		return bodyPart;
	}
	/**
	 * If true, this layer can't be edited as clothing
	 */
	public LayerDef setBodyPart(boolean bodyPart) {
		this.bodyPart = bodyPart;
		return this;
	}


	/**
	 * Change the way the clothing is displayed in gui:
	 * 0 > normal, full body height is visible
	 * 1 > bigger, a bit zoomed in, still centered
	 * 2 > shoes, even more zoomed in, offseted for legs display
	 * @return
	 */
	public int getDisplayMode() {
		return displayMode;
	}

	public LayerDef setDisplayMode(int displayMode) {
		this.displayMode = displayMode;
		return this;
	}
	public NbtCompound toNBT() {
		var res = new NbtCompound();
		res.putString("id", id);
		res.putInt("layer1", layer1);
		res.putInt("layer2", layer2);
		res.putBoolean("forced", forced);
		res.putBoolean("baseLayer", baseLayer);
		res.putBoolean("bodyPart", bodyPart);
		res.putInt("displayMode", displayMode);
		return res;
	}

	public static LayerDef fromNBT(NbtCompound comp) {
		try {
			return new LayerDef(comp.getString("id"), comp.getInt("layer1")).setLayer2(comp.getInt("layer2")).setBaseLayer(comp.getBoolean("baseLayer")).setForced(comp.getBoolean("forced")).setBodyPart(comp.getBoolean("bodyPart")).setDisplayMode(comp.getInt("dispalyMode"));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
