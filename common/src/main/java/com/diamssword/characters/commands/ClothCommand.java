package com.diamssword.characters.commands;

import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.storage.ClothingLoader;
import com.diamssword.characters.api.ComponentManager;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ClothCommand {
	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
		var set=ClothingLoader.instance.getClothIds().stream().map(Identifier::toString).collect(Collectors.toSet());
		set.add("character_sheet:all");
		return CommandSource.suggestMatching(set, builder);
	};
	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER_LAYER = (context, builder) -> {
		var set=new HashSet<>(ClothingLoader.instance.getLayers().keySet());
		set.add("\"*\"");
		return CommandSource.suggestMatching(set, builder);
	};

	public static void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
		var root = builder.requires(ctx -> ctx.hasPermissionLevel(2));
		for (var str : new String[]{"unlock", "lock", "set"}) {
			root.then(CommandManager.literal(str).then(CommandManager.argument("player", EntityArgumentType.players()).then(CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER)
							.executes(ctx -> Exec(ctx, str)))));
		}
		root.then(CommandManager.literal("remove").then(CommandManager.argument("player", EntityArgumentType.players()).then(CommandManager.argument("layer", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER_LAYER)
						.executes(ClothCommand::ExecRemove))));
	}
	private static boolean execPerCloth(Identifier cloth, BiConsumer<Identifier, Cloth> consumer)
	{
		if( cloth.getPath().equals("all"))
		{
			var clothsId = ClothingLoader.instance.getClothIds().stream().filter(v->v.getNamespace().equals(cloth.getNamespace()));
			clothsId.forEach(id->{
				ClothingLoader.instance.getCloth(id).ifPresent(a->consumer.accept(id,a));
			});
			return true;
		}
		else
		{
			var c=ClothingLoader.instance.getCloth(cloth);
			if(c.isPresent())
			{
				consumer.accept(cloth,c.get());
				return true;
			}
			return false;
		}
	}
	private static boolean execPerLayer(String layername, BiConsumer<String,LayerDef> consumer)
	{
		if(layername.equals("*"))
		{
			ClothingLoader.instance.getLayers().forEach(consumer);
			return true;
		}
		else
		{
			var layer = ClothingLoader.instance.getLayer(layername);
			if(layer.isPresent())
			{
				consumer.accept(layername,layer.get());
				return true;
			}
			return false;
		}
	}
	private static int ExecRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var players = EntityArgumentType.getPlayers(ctx, "player");

		String layerID = StringArgumentType.getString(ctx, "layer");
		var bl=false;
		for (var player : players) {
			var app = ComponentManager.getPlayerDatas(player).getAppearence();
			if(execPerLayer(layerID, (l, layer) -> {
				app.removeCloth(l);
				ctx.getSource().sendFeedback(() -> Text.literal("").append(player.getDisplayName()).append(" is now naked in the cloth layer: " +l), true);
			})) {
				ComponentManager.syncPlayerDatas(player);
				bl=true;
			} else
				throw new SimpleCommandExceptionType(new LiteralMessage("Layer not found with id: " + layerID)).create();
		}
		return bl?1:-1;
	}
	private static int Exec(CommandContext<ServerCommandSource> ctx, String action) throws CommandSyntaxException {
		var players = EntityArgumentType.getPlayers(ctx, "player");
		Identifier clothID = IdentifierArgumentType.getIdentifier(ctx, "id");
		var bl=false;
		for (var player : players) {
			var app = ComponentManager.getPlayerDatas(player).getAppearence();
			if(execPerCloth(clothID,(id,cloth)->{
				switch (action) {
					case "unlock" -> {
						app.unlockCloth(cloth);
						ctx.getSource().sendFeedback(() -> Text.literal("").append(player.getDisplayName()).append(" have unlocked the cloth: " + id), true);
					}
					case "lock" -> {
						app.lockCLoth(cloth);
						ctx.getSource().sendFeedback(() -> Text.literal("").append(player.getDisplayName()).append(" have lost the cloth: " + id), true);
					}
					case "set" -> {
						app.setCloth(cloth);
						ctx.getSource().sendFeedback(() -> Text.literal("").append(player.getDisplayName()).append(" is now wearing: " + id), true);
					}
				}
			})) {
				ComponentManager.syncPlayerDatas(player);
				bl=true;
			}
			else
				throw new SimpleCommandExceptionType(new LiteralMessage("Cloth not found with id: " + clothID)).create();


		}
		return bl?1:-1;
	}
}