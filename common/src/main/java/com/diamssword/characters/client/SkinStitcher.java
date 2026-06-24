package com.diamssword.characters.client;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.http.SkinLayerValue;
import com.diamssword.characters.storage.BodyPartsLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class SkinStitcher {

	static MinecraftClient client=MinecraftClient.getInstance();
	public static BufferedImage createSkin(SkinLayerValue[] layers) throws IOException {
		BufferedImage main=new BufferedImage(BodyPartsLoader.instance.getSkinResolution(),BodyPartsLoader.instance.getSkinResolution(),BufferedImage.TYPE_INT_ARGB);
		var g=main.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setComposite(AlphaComposite.SrcOver);

		for(SkinLayerValue layer : layers) {
				var texture = getTexture(layer);
				if(texture != null) {
					boolean sided="left".equals(layer.side)||"right".equals(layer.side);
					if(!sided) {
						g.drawImage(texture, 0, 0, main.getWidth(), main.getHeight(), 0, 0, texture.getWidth(), texture.getHeight(), null);
					}
					else
					{
						if("left".equals(layer.side)) {
							//layer 1 left
							g.drawImage(texture, 0, 0, (int) (main.getWidth()*0.1875), (int) (main.getHeight()*0.25), 0, 0, (int) (texture.getWidth()*0.1875), (int) (texture.getHeight()*0.25), null);
							//layer 1 under
							g.drawImage(texture, (int)(main.getWidth()*0.25), 0, (int) (main.getWidth()*0.3125), (int) (main.getHeight()*0.125), (int)(texture.getWidth()*0.25), 0, (int) (texture.getWidth()*0.3125), (int) (texture.getHeight()*0.125), null);
							//layer 1 back
							g.drawImage(texture, (int)(main.getWidth()*0.4375), (int)(main.getHeight()*0.125), (int) (main.getWidth()*0.5), (int) (main.getHeight()*0.25), (int)(texture.getWidth()*0.4375),(int)(texture.getHeight()*0.125), (int) (texture.getWidth()*0.5), (int) (texture.getHeight()*0.25), null);
							//layer 2 left
							g.drawImage(texture, (int) (main.getWidth()*0.5), 0, (int) (main.getWidth()*(0.6875)), (int) (main.getHeight()*0.25),  (int) (texture.getWidth()*0.5), 0, (int) (texture.getWidth()*(0.6875)), (int) (texture.getHeight()*0.25), null);
							//layer 2 under
							g.drawImage(texture, (int)(main.getWidth()*0.75), 0, (int)(main.getWidth()*0.8125), (int) (main.getHeight()*0.125), (int)(texture.getWidth()*0.75), 0, (int)(texture.getWidth()*0.8125), (int) (texture.getHeight()*0.125), null);
							//layer 2 back
							g.drawImage(texture, (int)(main.getWidth()*0.9375), (int)(main.getHeight()*0.125), (int) (main.getWidth()), (int) (main.getHeight()*0.25), (int)(texture.getWidth()*0.9375),(int)(texture.getHeight()*0.125), (int) (texture.getWidth()), (int) (texture.getHeight()*0.25), null);
						}
						else
						{
							//layer 1 top right
							g.drawImage(texture, (int)(main.getWidth()*0.1875), 0, (int) (main.getWidth()*0.25), (int) (main.getHeight()*0.125),  (int)(texture.getWidth()*0.1875), 0, (int) (texture.getWidth()*0.25), (int) (texture.getHeight()*0.1875), null);
							//layer 1 front
							g.drawImage(texture, (int)(main.getWidth()*0.1875), (int)(main.getHeight()*0.125), (int) (main.getWidth()*0.4375), (int) (main.getHeight()*0.25),  (int)(texture.getWidth()*0.1875),  (int)(texture.getHeight()*0.125), (int) (texture.getWidth()*0.4375), (int) (texture.getHeight()*0.25), null);
							//layer 1 bottom
							g.drawImage(texture, (int)(main.getWidth()*0.3125), 0, (int) (main.getWidth()*0.375), (int) (main.getHeight()*0.125),  (int)(texture.getWidth()*0.3125),  0, (int) (texture.getWidth()*0.375), (int) (texture.getHeight()*0.125), null);
							//layer 2 top right
							g.drawImage(texture, (int)(main.getWidth()*0.6875), 0, (int) (main.getWidth()*0.75), (int) (main.getHeight()*0.125),  (int)(texture.getWidth()*0.6875), 0, (int) (texture.getWidth()*0.75), (int) (texture.getHeight()*0.1875), null);
							//layer 2 front
							g.drawImage(texture, (int)(main.getWidth()*0.6875), (int)(main.getHeight()*0.125), (int) (main.getWidth()*0.9375), (int) (main.getHeight()*0.25),  (int)(texture.getWidth()*0.6875),  (int)(texture.getHeight()*0.125), (int) (texture.getWidth()*0.9375), (int) (texture.getHeight()*0.25), null);
							//layer 2 bottom
							g.drawImage(texture, (int)(main.getWidth()*0.8125), 0, (int) (main.getWidth()*0.875), (int) (main.getHeight()*0.125),  (int)(texture.getWidth()*0.8125),  0, (int) (texture.getWidth()*0.875), (int) (texture.getHeight()*0.125), null);
						}
					}
				}

		}
		g.dispose();
		return main;
	}
	public static InputStream toInputStream(BufferedImage image) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			ImageIO.write(image, "png", baos);

			return new ByteArrayInputStream(baos.toByteArray());

		} catch (IOException e) {
			throw new RuntimeException("Failed to encode BufferedImage", e);
		}
	}
	public static NativeImage toNative(BufferedImage image) {

		NativeImage nativeImage = new NativeImage(
				NativeImage.Format.RGBA,
				image.getWidth(),
				image.getHeight(),
				false
		);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {

				int argb = image.getRGB(x, y);

				int a = (argb >> 24) & 0xFF;
				int r = (argb >> 16) & 0xFF;
				int g = (argb >> 8) & 0xFF;
				int b = argb & 0xFF;

				// convert ARGB → ABGR
				int abgr = (a << 24) | (b << 16) | (g << 8) | r;

				nativeImage.setColor(x, y, abgr);
			}
		}

		return nativeImage;
	}
	public static BufferedImage getTexture(SkinLayerValue layer)
	{
		var texture=layer.getTexturePath();
		int i=texture.indexOf(":");
		var path=texture;
		var id= BodyPartsLoader.instance.getDefaultDomain();
		if(i!=-1)
		{
			id=texture.substring(0,i);
			path=texture.substring(i+1);
		}
		var ress=client.getResourceManager().getResource(new Identifier(id,"textures/bodyparts/"+path+".png"));
		if(ress.isPresent())
		{
			try (InputStream stream = ress.get().getInputStream()) {
				return ImageIO.read(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
