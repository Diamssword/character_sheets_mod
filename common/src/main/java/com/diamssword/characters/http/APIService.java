package com.diamssword.characters.http;

import com.diamssword.characters.Characters;
import com.diamssword.characters.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class APIService {
	private static String token;

	/**
	 * Authentifie ou ré-autentifie le serveur à l'API
	 *
	 * @return true si l'auth est un succés
	 */
	public static CompletableFuture<Boolean> login() {
		var ob = new JsonObject();
		ob.addProperty("key", Characters.config.serverOptions.ServerSideApiKey);
		return postRequest(Characters.config.serverOptions.SkinServerURL + "/api/auth", "", ob).thenApply(rep -> {
			if (rep.statusCode() == 200) {

				token = JsonParser.parseString(rep.body()).getAsJsonObject().get("token").getAsString();
				return true;
			}
			return false;
		});
	}

	/**
	 * Use a code to validate a skin to the api_server
	 *
	 * @param player
	 * @param code
	 * @return true if the skin was processed
	 */
	public static CompletableFuture<Optional<ApiCharacterValues>> importCharacter(PlayerEntity player, String code) {
		var ob = new JsonObject();
		ob.addProperty("code", code);
		ob.addProperty("uuid", player.getGameProfile().getId().toString());

		return postRequest(Characters.config.serverOptions.SkinServerURL + "/api/player/validate", token, ob).thenApply(v -> {
			if (v.statusCode() != 200) {
				try {
					return login().thenApply(c -> {
						if (!c)
							return Optional.<ApiCharacterValues>empty();
						else {
							try {
								return importCharacter(player, code).get();
							} catch (ExecutionException | InterruptedException e) {
								return Optional.<ApiCharacterValues>empty();
							}
						}
					}).get();
				} catch (ExecutionException | InterruptedException e) {
					return Optional.empty();
				}
			} else {
				var val = new Gson().fromJson(v.body(), ApiCharacterValues.class);
				return Optional.of(val);
			}
		});
	}

	public static CompletableFuture<Optional<Pair<Boolean, UUID>>> getInfosForUsername(String name) {
		return getRequest(Characters.config.serverOptions.SkinServerURL + "/api/player/get/" + name, null).thenApply(v -> {
			if (v.statusCode() == 200) {
				var s = JsonParser.parseString(v.body());
				var uis = s.getAsJsonObject().get("uuid").getAsString();
				var b = s.getAsJsonObject().get("exist").getAsBoolean();
				return Optional.of(new Pair<>(b, Utils.parseUUID(uis)));
			}
			return Optional.empty();
		});
	}

	public static CompletableFuture<HttpResponse<String>> getRequest(String url, String token) {

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET()
				.header("Content-Type", "application/json")
				.header("Authorization", token == null ? "" : token)
				.build();
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

	}

	public static CompletableFuture<HttpResponse<String>> postRequest(String url, String token, JsonObject body) {

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.POST(HttpRequest.BodyPublishers.ofString(body.toString()))
				.header("Content-Type", "application/json")
				.header("Authorization", token == null ? "" : token)
				.build();
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

	}
}
