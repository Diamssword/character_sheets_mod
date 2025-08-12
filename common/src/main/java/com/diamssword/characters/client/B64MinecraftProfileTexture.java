package com.diamssword.characters.client;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class B64MinecraftProfileTexture {


		private final String data;
		private final Map<String, String> metadata;

		public B64MinecraftProfileTexture(final String data, final Map<String, String> metadata) {
			this.data = data;
			this.metadata = metadata;
		}

		public String getData() {
			return data;
		}

		@Nullable
		public String getMetadata(final String key) {
			if (metadata == null) {
				return null;
			}
			return metadata.get(key);
		}

		public String getHash() {
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
				return bytesToHex(hashBytes);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("SHA-256 not supported", e);
			}
		}

		// Convertit un tableau de bytes en hexadécimal
		private static String bytesToHex(byte[] bytes) {
			StringBuilder hexString = new StringBuilder(2 * bytes.length);
			for (byte b : bytes) {
				hexString.append(String.format("%02x", b));
			}
			return hexString.toString();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("base64", data)
					.append("hash", getHash())
					.toString();
		}
	}