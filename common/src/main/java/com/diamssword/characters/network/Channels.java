package com.diamssword.characters.network;

import com.diamssword.characters.Characters;
import com.diamssword.characters.ClothingLoader;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.network.packets.DictionaryPackets;
import com.diamssword.characters.network.owoNetwork.OwoNetChannel;
import com.diamssword.characters.network.owoNetwork.serialization.PacketBufSerializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

public class Channels {
	public static final OwoNetChannel MAIN = OwoNetChannel.create(Characters.asRessource("channel"));
	public static void init()
	{
		MAIN.init();
		PacketBufSerializer.register(ClothingLoader.class, ClothingLoader::serializer, ClothingLoader::unserializer);
		SkinServerCache.init();
		CosmeticsPackets.init();
		DictionaryPackets.init();
	}


	public static boolean isSelfHost(MinecraftServer server, PlayerEntity player) {
		return server.isHost(player.getGameProfile());
	}

	/**
	 * IntegratedServer safe version, doesn't send packet to the player hosting;
	 *
	 * @param server
	 * @return
	 */
	public static OwoNetChannel.ServerHandle serverHandle(MinecraftServer server) {
		return MAIN.serverHandle(server.getPlayerManager().getPlayerList().stream().filter(v -> !Channels.isSelfHost(server, v)).toList());
	}

	/**
	 * IntegratedServer safe version, doesn't send packet to the player if he his hosting;
	 *
	 * @param player
	 * @return
	 */
	public static <R extends Record> void sendToNonHost(PlayerEntity player, R... messages) {
		if (!isSelfHost(player.getServer(), player)) {
			MAIN.serverHandle(player).send(messages);
		}
	}
}
