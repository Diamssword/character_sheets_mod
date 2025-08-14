package com.diamssword.characters.network.packets;

import com.diamssword.characters.storage.ClassesLoader;
import com.diamssword.characters.storage.ClothingLoader;
import com.diamssword.characters.client.CharactersClient;
import com.diamssword.characters.network.Channels;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class DictionaryPackets {
	public record ClothingList(ClothingLoader loader) {
	}
	public record ClassesList(ClassesLoader loader) {
	}
	public static void init() {

		Channels.MAIN.registerClientbound(ClothingList.class, (msg, ctx) -> {
			ClothingLoader.instance = msg.loader;
			if(Platform.getEnvironment()==Env.CLIENT)
				reloadClientModel();
		});
		Channels.MAIN.registerClientbound(ClassesList.class, (msg, ctx) -> {
			ClassesLoader.instance = msg.loader;
		});
	}
	@Environment(EnvType.CLIENT)
	public static void reloadClientModel()
	{
		CharactersClient.reloadPlayerRender();
	}
}
