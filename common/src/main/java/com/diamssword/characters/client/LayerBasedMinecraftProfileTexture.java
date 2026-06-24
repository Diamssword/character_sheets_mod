package com.diamssword.characters.client;

import com.diamssword.characters.api.http.SkinLayerValue;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class LayerBasedMinecraftProfileTexture {


		private final SkinLayerValue[] data;
		private final Map<String, String> metadata;

		public LayerBasedMinecraftProfileTexture(final SkinLayerValue[] layers, final Map<String, String> metadata) {
			this.data = layers;
			this.metadata = metadata;
		}

		public SkinLayerValue[] getData() {
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
				for (SkinLayerValue v : data) {
					digest.update(v.toString().getBytes(StandardCharsets.UTF_8));
				}
				return bytesToHex(digest.digest());
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("SHA-256 not supported", e);
			}
		}

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