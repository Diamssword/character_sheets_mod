package com.diamssword.characters.api;

import com.diamssword.characters.api.http.SkinLayerValue;
import com.diamssword.characters.api.skin.BodyLayerDictionary;
import com.diamssword.characters.api.skin.BodyLayerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;

public record PlayerSkinInfos(String characterName,String username,SkinLayerValue[] layers, boolean slim) {
		public static PlayerSkinInfos fromNBT(NbtCompound tag)
		{
			var ls=tag.getList("layers", NbtElement.COMPOUND_TYPE);
			SkinLayerValue[] arr=new SkinLayerValue[ls.size()];
			for(int i = 0; i < ls.size(); i++) {
				arr[i]=new SkinLayerValue().fromNBT(ls.getCompound(i));
			}
			return new PlayerSkinInfos(tag.getString("characterName"),tag.getString("username"),arr,tag.getBoolean("slim"));
		}
		public NbtCompound toNBT()
		{
			NbtCompound tag=new NbtCompound();
			tag.putString("characterName",characterName);
			tag.putString("username",username);
			tag.putBoolean("slim",slim);
			var ls=new NbtList();
			for(SkinLayerValue layer : layers) {
				ls.add(layer.toNBT());
			}
			tag.put("layers",ls);
			return tag;
		}

	}