package com.diamssword.characters.api.skin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record BodyLayerImageGroup(String id, BodyLayerImageGroup[] subs) {

	public Optional<BodyLayerImageGroup> filterImageGroups(String search)
	{
		var s=search.toLowerCase();
		if(search.isBlank() ||id.toLowerCase().contains(s))
		{
			return Optional.of(this);
		}
		if(hasSubs())
		{
			List<BodyLayerImageGroup> ls=new ArrayList<>();
			for(BodyLayerImageGroup sub : subs) {
				if(sub.id.toLowerCase().contains(s))
					ls.add(sub);
			}
			if(!ls.isEmpty())
			{
				return Optional.of(new BodyLayerImageGroup(id,ls.toArray(new BodyLayerImageGroup[0])));
			}
		}
		return Optional.empty();
	}
	public boolean hasSubs()
	{
		return subs !=null && subs.length>0;
	}
	public NbtCompound toNBT()
	{
		var tag=new NbtCompound();
		tag.putString("id",id);
		if(subs!=null && subs.length>0)
		{
			var ls=new NbtList();
			for(BodyLayerImageGroup sub : subs) {
				ls.add(sub.toNBT());
			}
			tag.put("subs",ls);
		}
		return tag;
	}
	@Nullable
	public static BodyLayerImageGroup fromNBT(NbtCompound tag)
	{

		if(tag.contains("id"))
		{
			var id=tag.getString("id");
			if(tag.contains("subs"))
			{
				var ls=tag.getList("subs", NbtElement.COMPOUND_TYPE);
						var arr=new BodyLayerImageGroup[ls.size()];
				for(int i = 0; i < arr.length; i++) {
					arr[i]=fromNBT(ls.getCompound(i));
				}
				return new BodyLayerImageGroup(id,arr);
			}
			return new BodyLayerImageGroup(id,new BodyLayerImageGroup[0]);
		}
		return null;
	}
}
