package com.diamssword.characters.fabric;

import com.diamssword.characters.network.Channels;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class WardrobeCommand {

	public static void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
		builder.requires(ctx -> ctx.hasPermissionLevel(2)).then(CommandManager.argument("player", EntityArgumentType.players()).executes((e)->{
			var players=EntityArgumentType.getPlayers(e,"player");
			Channels.MAIN.serverHandle(players).send(new GuiPackets.WardRobePacket());
			return 1;
		}));

	}
}
