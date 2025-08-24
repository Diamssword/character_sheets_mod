package com.diamssword.characters.commands;

import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.GuiPackets;
import com.diamssword.characters.storage.ClothingLoader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WardrobeCommand {
	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
		if (context.getSource().isExecutedByPlayer()) {
			Set<String> res=new HashSet<>();
			res.add("default");
			ClothingLoader.instance.getLayers().values().forEach(v->{
				if(v.getSpecialEditor() !=null)
					res.add(v.getSpecialEditor());
			});
			return CommandSource.suggestMatching(res, builder);
		}
		return CommandSource.suggestMatching(new String[0], builder);

	};
	public static void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
		builder.requires(ctx -> ctx.hasPermissionLevel(2)).then(CommandManager.argument("player", EntityArgumentType.players()).executes((e)->{
			var players=EntityArgumentType.getPlayers(e,"player");
			Channels.MAIN.serverHandle(players).send(new GuiPackets.WardRobePacket("default"));
			return 1;
		}).then(CommandManager.argument("type", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER).executes(ctx->{
			var players=EntityArgumentType.getPlayers(ctx,"player");
			var type=StringArgumentType.getString(ctx,"type");
			Channels.MAIN.serverHandle(players).send(new GuiPackets.WardRobePacket(type));
			return 1;
		})));

	}
}
