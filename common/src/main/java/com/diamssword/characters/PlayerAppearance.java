package com.diamssword.characters;

import com.diamssword.characters.api.Cloth;
import com.diamssword.characters.api.IPlayerAppearance;
import com.diamssword.characters.api.LayerDef;
import com.diamssword.characters.http.ApiSkinValues;
import com.diamssword.characters.storage.ComponentManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerAppearance implements IPlayerAppearance {
	private float scaledHeight = 1f;
	private final Map<String, Cloth> cloths = new HashMap<>();
	private final PlayerEntity parent;
	private final List<String> unlockedCloths = new ArrayList<>();
	private final SavedOutfit[] outfits = new SavedOutfit[7];
	private ApiSkinValues skinDatas;

	public PlayerAppearance(PlayerEntity parent) {
		this.parent = parent;
		if (!parent.getWorld().isClient && parent.getGameProfile() != null)
			refreshSkinData();
		fillForcedLayers();
	}

	public void tick() {
		if (skinDatas == null && !parent.getWorld().isClient && parent.age % 20 == 0 && parent.getGameProfile() != null)
			refreshSkinData();
	}

	public ApiSkinValues getSkinDatas() {
		return skinDatas;
	}

	public void refreshSkinData() {
		var car = ComponentManager.getPlayerCharacter(parent).getCurrentCharacter();
		if (car != null) {
			skinDatas = car.appearence;
			if (skinDatas != null) {
				scaledHeight = ApiSkinValues.HeightMToMCScale(1, skinDatas.size);
				ComponentManager.syncPlayerDatas(parent);
			}

		}
	}

	@Override
	public void clonePlayerAppearance(PlayerEntity sourcePlayer) {
		var car = ComponentManager.getPlayerDatas(sourcePlayer).getAppearence();
		if (car != null) {
			var tag=new NbtCompound();
			car.writeToNbt(tag,false);
			this.readFromNbt(tag);

		}
	}

	public void unlockCloth(Cloth cloth) {
		if (!unlockedCloths.contains(cloth.layer().id+"_"+cloth.id()))
			unlockedCloths.add(cloth.layer().id+"_"+cloth.id());
	}

	@Override
	public void lockCLoth(Cloth cloth) {
		unlockedCloths.remove(cloth.layer().id+"_"+cloth.id());
	}
	@Override
	public ArrayList<String> getUnlockedCloths() {
		return new ArrayList<>(unlockedCloths);
	}

	@Override
	public Optional<Cloth> getEquippedCloth(LayerDef layer) {
		return  Optional.ofNullable(cloths.get(layer.getId()));
	}

	@Override
	public Map<String, Cloth> getEquippedCloths() {
		var res = new HashMap<>(cloths);
		for (LayerDef layer : ClothingLoader.instance.getClothLayers()) {
			if (!res.containsKey(layer.getId()))
				res.put(layer.getId(), null);
		}
		return res;
	}
	public record ClothData(String texture, boolean needColor, int color) {
	}
	private void fillForcedLayers()
	{
		ClothingLoader.instance.getLayers().values().stream().filter(LayerDef::isForced).forEach(la->{
			var und=this.getEquippedCloth(la);
			if(und.isEmpty())
			{
				var l=ClothingLoader.instance.getForLayers(la);
				if(!l.isEmpty())
					this.setCloth(l.get(0));
			}
		});


	}
	public Optional<ClothData> getClothDatas(LayerDef layer) {
		return getEquippedCloth(layer).map(v -> new ClothData(v.id(), false, -1));
	}
	@Override
	public boolean equipCloth(Cloth cloth) {
		if (cloth == null) {
			return false;
		}
		if (this.parent.getWorld().isClient || this.parent.isCreative() || this.unlockedCloths.contains(cloth.layer().getId()+"_"+cloth.id())) {
			setCloth(cloth.layer().id, cloth);
			return true;
		} else
			return false;
	}
	@Override
	public void setCloth(String layer, @Nullable Cloth cloth) {
		if (cloth == null)
			this.cloths.remove(layer);
		else
			this.cloths.put(layer, cloth);
		ComponentManager.syncPlayerDatas(parent);

	}
	@Override
	public void setCloth(Cloth cloth) {
		if (cloth != null)
			setCloth(cloth.layer().getId(), cloth);

	}
	@Override
	public void removeCloth(String layer) {
			this.setCloth(layer,null);
	}
	@Override
	public void saveOutfit(String name, int index) {
		if (index < this.outfits.length && index >= 0) {
			this.outfits[index] = new SavedOutfit(name,this);
			this.outfits[index].populate();
		}
	}

	@Override
	public List<Pair<String,Integer>> getOutfits() {
		var res = new ArrayList<Pair<String,Integer>>();
		for (var i=0;i<outfits.length;i++)
		{
			if (outfits[i] != null)
				res.add(new Pair<>(outfits[i].name,i));
		}
		return res;
	}
@Override
	public void equipOutfit(int index) {
		if (index < this.outfits.length && index >= 0 && this.outfits[index] != null) {

			this.outfits[index].equipe();
		}
	}

	@Override
	public float getHeightScale() {
			return Math.max(0.8f, Math.min(1.1f, scaledHeight));
	}

	public void readFromNbt(NbtCompound tag) {
		if (tag.contains("default")) {
			this.skinDatas = new ApiSkinValues().fromNBT(tag.getCompound("default"));
			scaledHeight = ApiSkinValues.HeightMToMCScale(1, skinDatas.size);
		}
		if (tag.contains("cloths")) {
			cloths.clear();
			var cl = tag.getCompound("cloths");
			cl.getKeys().forEach(k -> {
				try {
					var cl1 = ClothingLoader.instance.getCloth(cl.getString(k));
					cl1.ifPresent(v -> cloths.put(v.layer().getId(), v));
				} catch (Exception ignored) {
				}
			});
		}
		if (tag.contains("unlockedCloths")) {
			unlockedCloths.clear();
			var cl = tag.getList("unlockedCloths", NbtElement.COMPOUND_TYPE);
			cl.forEach(k -> unlockedCloths.add(((NbtCompound) k).getString("id")));
		}
		if (tag.contains("outfits")) {
			var cl = tag.getList("outfits", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < cl.size(); i++) {
				if (i < this.outfits.length) {
					if (((NbtCompound) cl.get(i)).isEmpty())
						outfits[i] = null;
					else
						outfits[i] = new SavedOutfit("",this).fromNBT(((NbtCompound) cl.get(i)));
				}
			}
		}
		fillForcedLayers();
	}

	public NbtCompound writeToNbt(NbtCompound tag, boolean forClient) {
		if (skinDatas != null && forClient)
			tag.put("default", skinDatas.toNBT());
		var cloths = new NbtCompound();
		var unlocked = new NbtList();
		this.unlockedCloths.forEach(v -> {
			var t = new NbtCompound();
			t.putString("id", v);
			unlocked.add(t);
		});
		this.cloths.forEach((i, v) -> cloths.putString(i, v.layer().getId() + "_" + v.id()));
		tag.put("unlockedCloths", unlocked);
		tag.put("cloths", cloths);
		var outLs = new NbtList();
		for (SavedOutfit outfit : outfits) {
			if (outfit != null)
				outLs.add(outfit.toNBT());
			else
				outLs.add(new NbtCompound());
		}
		tag.put("outfits", outLs);
		return tag;
	}
	public static class SavedOutfit {
		public List<String> cloths = new ArrayList<>();
		public final String name;
		public final PlayerAppearance parent;

		public SavedOutfit(String name,PlayerAppearance parent) {
			this.name = name;
			this.parent=parent;
		}

		public void populate() {
			cloths.clear();
			parent.getEquippedCloths().forEach((l, c) -> {
				if (c != null)
					cloths.add(c.layer().getId() + "_" + c.id());
			});
		}

		public SavedOutfit fromNBT(NbtCompound tag) {
			var ls = tag.getList("cloths", NbtElement.COMPOUND_TYPE);
			ls.forEach(v -> {
				this.cloths.add(((NbtCompound) v).getString("id"));
			});
			return this;
		}

		public NbtCompound toNBT() {
			var res = new NbtCompound();
			res.putString("name", this.name);
			var ls = new NbtList();
			this.cloths.forEach(c -> {
				var c1 = new NbtCompound();
				c1.putString("id", c);
				ls.add(c1);
			});
			res.put("cloths", ls);
			return res;
		}

		public void equipe() {
			cloths.forEach(v -> {
				ClothingLoader.instance.getCloth(v).ifPresent(parent::equipCloth);
			});

		}
	}
}