package com.diamssword.characters.client;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.http.SkinLayerValue;
import com.diamssword.characters.client.renders.LayeredPlayer;
import com.diamssword.characters.mixins.PlayerSkinProviderAccessor;
import com.google.common.hash.Hashing;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class SingleLayerBasedTexture extends ResourceTexture {
	private static final Logger LOGGER = LogUtils.getLogger();
	@Nullable
	private final File cacheFile;
	private final SkinLayerValue layer;
	@Nullable
	private CompletableFuture<?> loader;
	private boolean loaded;

	protected SingleLayerBasedTexture(@Nullable File cacheFile, SkinLayerValue layer, Identifier fallbackSkin) {
		super(fallbackSkin);
		this.cacheFile = cacheFile;
		this.layer = layer;
	}
	private static File cache()
	{
		var d=((PlayerSkinProviderAccessor) MinecraftClient.getInstance().getSkinProvider()).getCacheDir();
		return new File(d.getParentFile(),"skinParts");
	}
	public static Identifier loadSkin(SkinLayerValue layer,int index) {

		Identifier identifier = Characters.asRessource("bodypart/" + layer.layer+"/"+index+(layer.side==null?"":layer.side));
			File file = new File(cache(), layer.layer+index);
			if (file.exists())
				file.delete();
			SingleLayerBasedTexture playerSkinTexture = new SingleLayerBasedTexture(file, layer, LayeredPlayer.CLEAR_TEXTURE);
			MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, playerSkinTexture);


		return identifier;
	}
	private void onTextureLoaded(NativeImage image) {
		MinecraftClient.getInstance().execute(() -> {
			this.loaded = true;
			if (!RenderSystem.isOnRenderThread()) {
				RenderSystem.recordRenderCall(() -> this.uploadTexture(image));
			} else {
				this.uploadTexture(image);
			}
		});
	}

	private void uploadTexture(NativeImage image) {
		TextureUtil.prepareImage(this.getGlId(), image.getWidth(), image.getHeight());
		image.upload(0, 0, 0, true);
	}

	@Override
	public void load(ResourceManager manager) throws IOException {
		MinecraftClient.getInstance().execute(() -> {
			if (!this.loaded) {
				try {
					super.load(manager);
				} catch (IOException var3x) {
					LOGGER.warn("Failed to load texture: {}", this.location, var3x);
				}

				this.loaded = true;
			}
		});
		if (this.loader == null) {
			NativeImage nativeImage;
			if (this.cacheFile != null && this.cacheFile.isFile()) {
				LOGGER.debug("Loading http texture from local cache ({})", this.cacheFile);
				FileInputStream fileInputStream = new FileInputStream(this.cacheFile);
				nativeImage = this.loadTexture(fileInputStream);
			} else {
				nativeImage = null;
			}

			if (nativeImage != null) {
				this.onTextureLoaded(nativeImage);
			} else {
				this.loader = CompletableFuture.runAsync(() -> {
					LOGGER.debug("Getting layered texture from {} to {}", this.layer, this.cacheFile);

					try {
						var texture=createTexture();
						if (this.cacheFile != null && texture != null) {
							FileUtils.copyInputStreamToFile(SkinStitcher.toInputStream(texture), this.cacheFile);
						}
						MinecraftClient.getInstance().execute(() -> {
							if (texture != null) {
								this.onTextureLoaded(SkinStitcher.toNative(texture));
							}
						});
					} catch (Exception var6) {
						LOGGER.error("Couldn't download http texture", var6);
					}
				}, Util.getMainWorkerExecutor());
			}
		}
	}
	@Nullable
	private NativeImage loadTexture(InputStream stream) {
		NativeImage nativeImage = null;

		try {
			nativeImage = NativeImage.read(stream);

		} catch (Exception var4) {
			LOGGER.warn("Error while loading the skin texture", var4);
		}

		return nativeImage;
	}
	@Nullable
	private BufferedImage createTexture() {
		BufferedImage image = null;
		try {

			image = SkinStitcher.createSkin(new SkinLayerValue[]{this.layer});
		} catch (Exception var4) {
			LOGGER.warn("Error while creating the skin texture", var4);
		}

		return image;
	}
}
