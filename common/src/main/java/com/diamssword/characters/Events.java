package com.diamssword.characters;

import com.diamssword.characters.api.IPlayerComponent;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.SkinServerCache;
import com.diamssword.characters.network.packets.DictionaryPackets;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.storage.BodyPartsLoader;
import com.diamssword.characters.storage.ClassesLoader;
import com.diamssword.characters.storage.ClothingLoader;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Events {
	public static void init() {
		PlayerEvent.PLAYER_JOIN.register((h) -> {
			Channels.sendToNonHost(h,new DictionaryPackets.ClothingList(ClothingLoader.instance),new DictionaryPackets.ClassesList(ClassesLoader.instance),new DictionaryPackets.BodyPartList(BodyPartsLoader.instance));
			ComponentManager.syncPlayerDatas(h);
			h.getWorld().getPlayers().forEach(pl->{
				ComponentManager.INSTANCE.syncComponent(pl,IPlayerComponent.class,h);
			});
		});
		TickEvent.SERVER_POST.register(ClothingLoader.instance::worldTick);
		TickEvent.SERVER_POST.register(BodyPartsLoader.instance::worldTick);
		TickEvent.SERVER_POST.register(ClassesLoader.instance::worldTick);
	}
}
