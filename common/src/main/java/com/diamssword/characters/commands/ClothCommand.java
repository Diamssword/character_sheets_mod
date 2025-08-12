package com.diamssword.characters.commands;

import com.diamssword.characters.ClothingLoader;
import com.diamssword.characters.http.APIService;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.storage.ComponentManager;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ClothCommand {
	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> CommandSource.suggestMatching(ClothingLoader.instance.getClothIds(), builder);
	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER_LAYER = (context, builder) -> CommandSource.suggestMatching(ClothingLoader.instance.getLayers().keySet(), builder);

	public static void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
		var root = builder.requires(ctx -> ctx.hasPermissionLevel(2));
		for (var str : new String[]{"unlock", "lock", "set"}) {
			root.then(CommandManager.literal(str).then(CommandManager.argument("id", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER)
					.then(CommandManager.argument("player", EntityArgumentType.players())
							.executes(ctx -> Exec(ctx, str)))));
		}
		root.then(CommandManager.literal("remove").then(CommandManager.argument("layer", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER_LAYER)
				.then(CommandManager.argument("player", EntityArgumentType.players())
						.executes(ClothCommand::ExecRemove))));
	}
	private static int ExecRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var players = EntityArgumentType.getPlayers(ctx, "player");

		String layerID = StringArgumentType.getString(ctx, "layer");
		var layer = ClothingLoader.instance.getLayer(layerID);
		if (layer.isPresent()) {
			for (var player : players) {
				var app = ComponentManager.getPlayerDatas(player).getAppearence();
				app.removeCloth(layerID);
				ctx.getSource().sendFeedback(() -> Text.literal("").append(player.getDisplayName()).append(" is now naked in the cloth layer: " +layerID), true);
				ComponentManager.syncPlayerDatas(player);
				return 1;
			}
		}
		throw new SimpleCommandExceptionType(new LiteralMessage("Layer not found with id: " + layerID)).create();
	}
	private static int Exec(CommandContext<ServerCommandSource> ctx, String action) throws CommandSyntaxException {


		var players = EntityArgumentType.getPlayers(ctx, "player");

		String clothID = StringArgumentType.getString(ctx, "id");
		var cloth = ClothingLoader.instance.getCloth(clothID);
		if (cloth.isPresent()) {
			for (var player : players) {
				var app = ComponentManager.getPlayerDatas(player).getAppearence();
				switch (action) {
					case "unlock" -> {
						app.unlockCloth(cloth.get());
						ctx.getSource().sendFeedback(() -> Text.literal("").append(player.getDisplayName()).append(" have unlocked the cloth: " + clothID), true);
					}
					case "lock" -> {
						app.lockCLoth(cloth.get());
						ctx.getSource().sendFeedback(() -> Text.literal("").append(player.getDisplayName()).append(" have lost the cloth: " + clothID), true);
					}
					case "set" -> {
						app.setCloth(cloth.get());
						ctx.getSource().sendFeedback(() -> Text.literal("").append(player.getDisplayName()).append(" is now wearing: " + clothID), true);
					}
				}
				ComponentManager.syncPlayerDatas(player);
				return 1;
			}
		}
		throw new SimpleCommandExceptionType(new LiteralMessage("Cloth not found with id: " + clothID)).create();
	}
}