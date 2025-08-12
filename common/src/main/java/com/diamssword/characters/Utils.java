package com.diamssword.characters;

import java.util.UUID;

public class Utils {

	public static UUID parseUUID(String uuid) {
		if (uuid.contains("-"))
			return UUID.fromString(uuid);
		return UUID.fromString(uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
	}

	public static String UUIDToString(UUID uuid) {
		return uuid.toString().replaceAll("-", "");
	}
}
