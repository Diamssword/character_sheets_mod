package com.diamssword.characters.client.renders;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.CharactersApi;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.api.http.SkinLayerValue;
import com.diamssword.characters.api.skin.BodyLayerInfo;
import com.diamssword.characters.storage.ClothingLoader;
import com.diamssword.characters.storage.PlayerAppearance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.BiConsumer;

public class LayeredPlayer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
	public final static Identifier CLEAR_TEXTURE=Characters.asRessource("textures/clear.png");
	int counter=0;
	Map<Integer, Pair<BodyPartLayer,BodyPartLayer>> indexToLayer=new HashMap<>();
	Map<Integer, Pair<BodyPartLayer,BodyPartLayer>> indexToLayerAlt=new HashMap<>();
	Pair<BodyPartLayer,BodyPartLayer> underwear;
	final boolean slim;
	public LayeredPlayer(EntityRendererFactory.Context ctx, boolean slim) {
		super(ctx,  new PlayerEntityModel<>(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), slim),0.5f);
		this.slim=slim;
		ClothingLoader.instance.getLayer("underwear").ifPresent(l->{
			underwear=new Pair<>(new BodyPartLayer(this, new BodyLayerInfo("underwear","Underwear",l.getLayer1()/1000f,false,false,false,true), false, slim,0),new BodyPartLayer(this, new BodyLayerInfo("underwear","Underwear",l.getLayer2()/1000f,false,false,false,true), true, slim,0));

			var plp=ComponentManager.getPlayerDatas(MinecraftClient.getInstance().player);
			if(plp.getAppearence() instanceof PlayerAppearance ap)
			{
			ap.getClothDatas(l).ifPresent(c->{
				underwear.getLeft().texture=underwear.getRight().texture=new Identifier(c.texture().getNamespace(),"textures/cloth/"+c.texture().getPath()+".png");
				if (l.getLayer2() <= -1)
					underwear.setRight(null);
			});
			}

		});

	}
	public void init(List<Pair<SkinLayerValue,SkinLayerValue>> layers, BiConsumer<Pair<SkinLayerValue,SkinLayerValue>,Integer> indexBinder)
	{
		features.clear();
		addFeature(underwear.getLeft());
		if(underwear.getRight()!=null)
			addFeature(underwear.getRight());
		counter=0;
		this.indexToLayer.clear();
		this.indexToLayerAlt.clear();
		for(Pair<SkinLayerValue,SkinLayerValue> layer : layers) {
				CharactersApi.bodyParts().getBodyLayer(layer.getLeft().layer).ifPresent(l -> {
					var ind = addLayer(l);
					setPart(layer.getLeft(), ind);
					indexBinder.accept(layer, ind);
					if(l.splited() && layer.getRight()!=null) {
						setPart(layer.getRight(),ind);
					}
				});
		}
	}

	public Identifier getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity) {
		return CLEAR_TEXTURE;
	}
	public void removeLayer(int layerIndex)
	{
		var pair=indexToLayer.get(layerIndex);
		if(pair !=null)
		{
			features.remove(pair.getRight());
			features.remove(pair.getLeft());
			indexToLayer.remove(layerIndex);
		}
		pair=indexToLayerAlt.get(layerIndex);
		if(pair !=null)
		{
			features.remove(pair.getRight());
			features.remove(pair.getLeft());
			indexToLayerAlt.remove(layerIndex);
		}
	}
	public int addLayer(BodyLayerInfo layer)
	{
		var c=counter;
		if(indexToLayer.containsKey(c))
		{
			features.remove(indexToLayer.get(c).getLeft());
			features.remove(indexToLayer.get(c).getRight());

		}
		if(indexToLayerAlt.containsKey(c))
		{
			features.remove(indexToLayerAlt.get(c).getLeft());
			features.remove(indexToLayerAlt.get(c).getRight());

		}
		counter++;
		var pair = new Pair<>(new BodyPartLayer(this, layer, false, slim, (int) (layer.size()*10000)+c), new BodyPartLayer(this, layer, true, slim, (int) (layer.size()*10000)+c));
		features.add(pair.getLeft());
		features.add(pair.getRight());
		indexToLayer.put(c,pair);
		if(layer.splited())
		{
			var pair1 = new Pair<>(new BodyPartLayer(this, layer, false, slim, (int) (layer.size()*10000)+c), new BodyPartLayer(this, layer, true, slim, (int) (layer.size()*10000)+c));
			features.add(pair1.getLeft());
			features.add(pair1.getRight());
			indexToLayerAlt.put(c,pair1);
		}
		return c;
	}
	public boolean setPart(SkinLayerValue layer,int index)
	{
		var l=indexToLayer.get(index);
		if(l!=null && layer!=null)
		{
			return CharactersApi.bodyParts().getBodyLayer(layer.layer).map(inf->{
				if(inf.splited())
				{
					if(!"right".equals(layer.side))
					{
						l.getRight().setPart(layer);
						l.getLeft().setPart(layer);
					}
					if(!"left".equals(layer.side))
					{
						var l1=indexToLayerAlt.get(index);
						if(l1!=null) {
							l1.getLeft().setPart(layer);
							l1.getRight().setPart(layer);
						}
						else
							return false;
					}
					return true;
				}
					l.getRight().setPart(layer);
					l.getLeft().setPart(layer);
				return true;
			}).orElse(false);
		}

		return false;
	}

	@Override
	protected boolean hasLabel(AbstractClientPlayerEntity livingEntity) {
		return false;
	}

}
