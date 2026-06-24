package com.diamssword.characters.api.skin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record BodyLayerDictionary(String id, String title, BodyLayerCategory[] cats) {
	public boolean hasSingleCategory()
	{
		return  cats==null || cats.length==0;
	}
	public List<Pair<BodyLayerCategory,List<BodyLayerImageGroup>>> filterImageGroups(String search)
	{
		List<Pair<BodyLayerCategory,List<BodyLayerImageGroup>>> ls=new ArrayList<>();
		for(BodyLayerCategory cat : cats) {
			var l=cat.filterImageGroups(search);
			if(!l.isEmpty())
				ls.add(new Pair<>(cat,l));
		}
		return ls;
	}
	public NbtCompound toNBT()
	{
		var tag=new NbtCompound();
		tag.putString("id",id);
		tag.putString("title",title);
		if(cats!=null && cats.length>0)
		{
			var ls=new NbtList();
			for(BodyLayerCategory sub : cats) {
				ls.add(sub.toNBT());
			}
			tag.put("cats",ls);
		}
		return tag;
	}
	@Nullable
	public static BodyLayerDictionary fromNBT(NbtCompound tag)
	{

		if(tag.contains("id"))
		{
			var id=tag.getString("id");
			var title=tag.getString("title");
			if(title.isBlank())
				title=id;
			if(tag.contains("cats"))
			{
				var ls=tag.getList("cats", NbtElement.COMPOUND_TYPE);
				var arr=new BodyLayerCategory[ls.size()];
				for(int i = 0; i < arr.length; i++) {
					arr[i]=BodyLayerCategory.fromNBT(ls.getCompound(i));
				}
				return new BodyLayerDictionary(id,title,arr);
			}
			return new BodyLayerDictionary(id,title,new BodyLayerCategory[0]);
		}
		return null;
	}
}
