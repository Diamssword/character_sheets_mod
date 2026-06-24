package com.diamssword.characters.api.skin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record BodyLayerCategory(String id, String title, BodyLayerImageGroup[] images) {
	public List<BodyLayerImageGroup> filterImageGroups(String search)
	{
		List<BodyLayerImageGroup> imgs=new ArrayList<>();
		for(var img : images) {
			img.filterImageGroups(search).ifPresent(imgs::add);
		}
		return imgs;
	}
	public NbtCompound toNBT()
	{
		var tag=new NbtCompound();
		tag.putString("id",id);
		tag.putString("title",title);
		if(images!=null && images.length>0)
		{
			var ls=new NbtList();
			for(BodyLayerImageGroup sub : images) {
				ls.add(sub.toNBT());
			}
			tag.put("images",ls);
		}
		return tag;
	}
	@Nullable
	public static BodyLayerCategory fromNBT(NbtCompound tag)
	{

		if(tag.contains("id"))
		{
			var id=tag.getString("id");
			var title=tag.getString("title");
			if(title.isBlank())
				title=id;
			if(tag.contains("images"))
			{
				var ls=tag.getList("images", NbtElement.COMPOUND_TYPE);
				var arr=new BodyLayerImageGroup[ls.size()];
				for(int i = 0; i < arr.length; i++) {
					arr[i]=BodyLayerImageGroup.fromNBT(ls.getCompound(i));
				}
				return new BodyLayerCategory(id,title,arr);
			}
			return new BodyLayerCategory(id,title,new BodyLayerImageGroup[0]);
		}
		return null;
	}
}
