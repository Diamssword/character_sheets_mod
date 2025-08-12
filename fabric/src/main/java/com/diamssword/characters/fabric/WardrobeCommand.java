package com.diamssword.characters.fabric;

import com.diamssword.characters.http.APIService;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.storage.ComponentManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class WardrobeCommand {

	public static void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
		builder.requires(ctx -> ctx.hasPermissionLevel(2)).then(CommandManager.argument("player", EntityArgumentType.players()).executes((e)->{
			var players=EntityArgumentType.getPlayers(e,"player");
			Channels.MAIN.serverHandle(players).send(new GuiPackets.WardRobePacket());
			return 1;
		}));

	}
}
