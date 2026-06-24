package com.diamssword.characters.api.skin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

public record BodyLayerInfo(String id, String title, float size, boolean clearable, boolean multi, boolean splited,boolean external) {

	public NbtCompound toNBT()
	{
		var tag=new NbtCompound();
		tag.putString("id",id);
		tag.putString("title",title);
		tag.putFloat("size",size);
		tag.putBoolean("clearable",clearable);
		tag.putBoolean("multi",multi);
		tag.putBoolean("splited",splited);
		tag.putBoolean("external",external);
		return tag;
	}
	@Nullable
	public static BodyLayerInfo fromNBT(NbtCompound tag)
	{

		if(tag.contains("id"))
		{
			var id=tag.getString("id");
			var title=tag.getString("title");
			var size=tag.getFloat("size");
			var clearable=tag.getBoolean("clearable");
			var splited=tag.getBoolean("splited");
			var multi=tag.getBoolean("multi");
			var external=tag.getBoolean("external");
			return new BodyLayerInfo(id,title,size,clearable,multi,splited,external);
		}
		return null;
	}
}
