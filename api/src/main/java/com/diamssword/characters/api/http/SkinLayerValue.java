package com.diamssword.characters.api.http;

import com.diamssword.characters.api.CharactersApi;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SkinLayerValue {
	public String layer;
	public String id;
	public String category;
	public String parent;
	public String side;

	public void toPacket(PacketByteBuf buf)
	{
		buf.writeString(layer);
		buf.writeString(id);
		buf.writeString(category==null?"":category);
		buf.writeString(parent==null?"":parent);
		buf.writeString(side==null?"":side);
	}
	public static SkinLayerValue fromPacket(PacketByteBuf buf)
	{
		var res=new SkinLayerValue();
		res.layer=buf.readString();
		res.id=buf.readString();
		var s=buf.readString();
		res.category= s.isBlank()?null:s;
		s=buf.readString();
		res.parent= s.isBlank()?null:s;
		s=buf.readString();
		res.side= s.isBlank()?null:s;
		return res;
	}
	public static SkinLayerValue createDefaultFor(String layer)
	{
		return CharactersApi.bodyParts().getBodyParts(layer).map(l->{
			var res=new SkinLayerValue();
			res.layer=l.id();

			var cat=l.cats()[0];
			if(!l.hasSingleCategory())
			{
				res.category= cat.id();
			}
			var img=cat.images()[0];
			if(img.hasSubs())
			{
				res.parent=img.id();
				res.id=img.subs()[0].id();
			}
			else
				res.id= img.id();
			return res;
		}).orElse(null);

	}
	@Override
	public String toString() {
		return layer+"|"+id+"|"+category+"|"+parent+"|"+side;
	}
	public String getTexturePath()
	{
		String str=layer+"/";
		if(this.category!=null)
			str=str+this.category+"/";
		if(this.parent!=null)
			str=str+this.parent+"/";
		return str+this.id;
	}
	public NbtCompound toNBT() {
		var tag = new NbtCompound();
		tag.putString("layer",layer);
		tag.putString("id",id);
		if(category!=null)
			tag.putString("category",category);
		if(parent!=null)
			tag.putString("parent",parent);
		if(side!=null)
		{
			if(side.equals("left"))
				tag.putBoolean("isLeft",true);
			else if(side.equals("right"))
				tag.putBoolean("isRight",true);
		}
		return tag;
	}

	public SkinLayerValue fromNBT(NbtCompound tag) {
		id=tag.getString("id");
		layer=tag.getString("layer");

		if(tag.contains("category"))
					category=tag.getString("category");
		else
			category=null;
		if(tag.contains("parent"))
			parent=tag.getString("parent");
		else
			parent=null;
		if(tag.getBoolean("isLeft"))
			side="left";
		else if(tag.getBoolean("isRight"))
			side="right";
		else
			side=null;
		return this;
	}
}
