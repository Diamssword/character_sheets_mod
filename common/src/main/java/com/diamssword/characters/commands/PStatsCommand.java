package com.diamssword.characters.commands;

import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.storage.ClassesLoader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

public class PStatsCommand {

	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {

		return CommandSource.suggestMatching(new String[]{"add", "set"}, builder);

	};
	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER3 = (context, builder) -> {

		return CommandSource.suggestMatching(new String[]{"xp", "level"}, builder);

	};
	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER1 = (context, builder) -> {

		return CommandSource.suggestMatching(ClassesLoader.getRoles().keySet(), builder);

	};

	public static void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
		var c1 = CommandManager.argument("player", EntityArgumentType.players())
				.then(CommandManager.argument("operation", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER)
						.then(CommandManager.argument("type", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER3)
								.then(CommandManager.argument("cat", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER1)
										.then(CommandManager.argument("count", IntegerArgumentType.integer()).executes(PStatsCommand::modExec)))));
		var c2 = CommandManager.argument("player", EntityArgumentType.players()).then(CommandManager.literal("get").then(CommandManager.argument("cat", StringArgumentType.string()).suggests(SUGGESTION_PROVIDER1).executes(PStatsCommand::getExec)));
		builder.requires(ctx -> ctx.hasPermissionLevel(2)).then(c1).then(c2);


	}

	private static int getExec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var players = EntityArgumentType.getPlayers(ctx, "player");
		String cat = StringArgumentType.getString(ctx, "cat");
		players.forEach(player -> {
			var stats = ComponentManager.getPlayerDatas(player).getStats();
			ctx.getSource().sendFeedback(() -> Text.literal(player.getName().getString() + " values for " + cat + ": Level[" + stats.getLevel(cat) + "] Xp[" + stats.getXp(cat) + "]"), false);
		});
		return 1;
	}

	private static int modExec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var players = EntityArgumentType.getPlayers(ctx, "player");
		String op = StringArgumentType.getString(ctx, "operation");
		String type = StringArgumentType.getString(ctx, "type");
		String cat = StringArgumentType.getString(ctx, "cat");
		int count = IntegerArgumentType.getInteger(ctx, "count");
		if (ClassesLoader.getRoles().containsKey(cat)) {
			players.forEach(player -> {
				var stats = ComponentManager.getPlayerDatas(player).getStats();
				stats.getOrCreate(cat, 0);
				if (type.equals("xp"))
					stats.setXp(cat, op.equals("set") ? count : stats.getXp(cat) + count);
				else if (type.equals("level"))
					stats.setLevel(cat, op.equals("set") ? count : stats.getLevel(cat) + count);

				ctx.getSource().sendFeedback(() -> Text.literal(player.getName().getString() + " " + cat + " value is now:  Level[" + stats.getLevel(cat) + "] Xp[" + stats.getXp(cat) + "]"), false);
				ComponentManager.syncPlayerDatas(player);
			});

			return 1;
		}
		return 0;


	}

}
