package com.diamssword.characters;

import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.SkinServerCache;
import com.diamssword.characters.network.packets.DictionaryPackets;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.storage.ClothingLoader;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;

public class Events {
	public static void init() {
		PlayerEvent.PLAYER_JOIN.register((h) -> {
			var car = ComponentManager.getPlayerCharacter(h).getCurrentCharacter();
			if (car != null)
				SkinServerCache.get(h.server).addToCache(h.getUuid(), car.base64Skin, car.base64SkinHead, car.appearence.slim);
			Channels.sendToNonHost(h,new DictionaryPackets.ClothingList(ClothingLoader.instance));
			ComponentManager.syncPlayerDatas(h);
		});
		PlayerEvent.PLAYER_QUIT.register(((player) -> {
			SkinServerCache.get(player.server).removeFromCache(player.getUuid());
		}));
		TickEvent.SERVER_POST.register(ClothingLoader.instance::worldTick);
	}
}
