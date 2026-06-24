package com.diamssword.characters.client.gui;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.CharactersApi;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.PlayerSkinInfos;
import com.diamssword.characters.api.http.SkinLayerValue;
import com.diamssword.characters.api.skin.BodyLayerCategory;
import com.diamssword.characters.api.skin.BodyLayerImageGroup;
import com.diamssword.characters.api.skin.BodyLayerInfo;
import com.diamssword.characters.client.SkinsLoader;
import com.diamssword.characters.client.gui.components.*;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class BodyPartEditorGui extends Screen {
	private final List<BaseComponent> components=new ArrayList<>();
	private final List<BodyLayerInfo> layerBts = new ArrayList<>();
	private LayerList layerListComp;
	private String lastSearch = "";
	private String currentLayer;
	private int currentIndex=0;
	private final Map<String,Map<Integer,Pair<SkinLayerValue,SkinLayerValue>>> layers=new HashMap<>();
	private final Map<String,Map<Integer,Integer>> layersLinkage=new HashMap<>();
	private final Map<String,Map<Integer,Boolean>> splittedState=new HashMap<>();
	private DrawerContainer<BodyPartButtonComponent.BodyPartRef,BodyPartButtonComponent> subDrawer;
	private Function<PlayerEntity,SkinLayerValue[]> equippedProvider;
	private final String saveId;
	public final String[] allowedLayers;
	public BodyPartEditorGui(SkinLayerValue[] preFilledLayers,String saveId,String... allowedLayers) {
		this(saveId,allowedLayers);
	this.equippedProvider=(p)-> CosmeticsPackets.mixandMatchLayers(MinecraftClient.getInstance().player,preFilledLayers,List.of(allowedLayers));
	}
	public BodyPartEditorGui(String saveId,String... allowedLayers) {
		super(Text.literal(""));
		this.saveId=saveId;
		this.allowedLayers=allowedLayers;
		var allL=List.of(allowedLayers);
		CharactersApi.bodyParts().getBodyLayers().forEach((k,v)->{
			if(allL.isEmpty() || allL.contains(k)) {
				if(currentLayer == null)
					currentLayer = k;
				if(!v.external())
					layerBts.add(v);
			}
		});
		equippedProvider=(p)->{
			var lo=SkinsLoader.clientSkinCache.getSkin(p.getUuid());
			return lo.map(PlayerSkinInfos::layers).orElse(new SkinLayerValue[0]);
		};

		//layers=List.of(equippedProvider.apply(MinecraftClient.getInstance().player));
	}
	private List<Pair<SkinLayerValue,SkinLayerValue>> layerArrayConverter(SkinLayerValue[] layers)
	{
		var list = new ArrayList<Pair<SkinLayerValue,SkinLayerValue>>();

			Map<String, List<SkinLayerValue>> rightSideds = new HashMap<>();
			List<String> added=new ArrayList<>();
			for(SkinLayerValue layer : layers) {

				if(!added.contains(layer.layer))
					added.add(layer.layer);
				if("right".equals(layer.side))
				{
					rightSideds.computeIfAbsent(layer.layer,k->new ArrayList<>()).add(layer);
				}
				else
				{
					list.add(new Pair<>(layer,null));
				}
			}
			CharactersApi.bodyParts().getBodyLayers().forEach((k,lay)->{
				if(!added.contains(k))
				{
					var lv=new SkinLayerValue();
					lv.layer=k;
					if(lay.clearable())
						lv.id="clear";
					else
						lv=SkinLayerValue.createDefaultFor(k);
					if(lay.splited())
					{
						lv.side="left";
						var lv1=new SkinLayerValue();
						lv1.id=lv.id;
						lv1.side="right";
						lv1.layer=lv.layer;
						lv1.category=lv.category;
						lv1.parent=lv.parent;
						var l1=new ArrayList<SkinLayerValue>();
						l1.add(lv1);
						rightSideds.put(k,l1);
					}
					list.add(new Pair<>(lv,null));
				}
			});
			list.forEach(p1->{
				var s=p1.getLeft();
				var l1=rightSideds.get(s.layer);
				if(l1!=null && !l1.isEmpty())
				{
					p1.setRight(l1.remove(0));
				}
			});
		return list;
	}
	public <T extends BaseComponent> T addComponent(T comp)
	{
		this.components.add(comp);
		addDrawable(comp);
		return comp;
	}
	public void updateDrawer(DrawerContainer<BodyPartButtonComponent.BodyPartRef,BodyPartButtonComponent> newDrawer)
	{
		if(this.subDrawer !=null)
		{
			remove(this.subDrawer);
		}
		this.subDrawer=newDrawer;
		if(this.subDrawer !=null)
			this.addDrawableChild(this.subDrawer);
	}


	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(subDrawer !=null)
		{
			if(subDrawer.mouseClicked(mouseX,mouseY,button))
				return true;
		}
		for (int i = children().size() - 1; i >= 0; i--) {
			Element element = children().get(i);
			if (element.mouseClicked(mouseX, mouseY, button)) {
				this.setFocused(element);
				if (button == 0) {
					this.setDragging(true);
				}

				return true;
			}
		}
		return false;
	}

	@Override
	public Optional<Element> hoveredElement(double mouseX, double mouseY) {
		Element overed=null;
		for (Element element : this.children()) {
			if (element.isMouseOver(mouseX, mouseY)) {
				if(element instanceof Widget dr)
				{
					if(overed instanceof Widget dr1)
					{
						if(dr.getWidth()<dr1.getWidth() && dr.getHeight()<dr1.getHeight())
							overed=element;
					}
					else
					{
						overed=element;
					}
				}
			}
		}
		return Optional.ofNullable(overed);
	}
	private SkinLayerValue[] flattenLayers()
	{
		List<SkinLayerValue> skin=new ArrayList<>();
		layers.forEach((k,v)->{
			v.forEach((k1,v1)->{
				if(v1.getLeft()!=null)
					skin.add(v1.getLeft());
				if(v1.getRight()!=null)
					skin.add(v1.getRight());
			});
		});
		return skin.toArray(new SkinLayerValue[0]);
	}
	private void exportSkin()
	{
		Channels.MAIN.clientHandle().send(new CosmeticsPackets.ApplySkinChange(flattenLayers()));
		this.close();
	}
	@Override
	protected void init() {
		super.init();
		this.layersLinkage.clear();
		this.components.clear();
		var playerComp =addComponent(new LayeredPlayerComponent(Sizing.fill(40),Sizing.content(0))).scaleToFit(true).lookAtCursor(false);

		int rightX=(int) (this.width*0.55f);
		int rightW=(int) (this.width*0.4f);
		layerListComp =addDrawableChild(new LayerList(rightX-25,(int) (this.height*0.2f),25,(int) (this.height*0.7f)));

		var scroll=this.addDrawableChild(new ScrollableBodyParts<BodyPartButtonComponent.BodyPartRef,BodyPartButtonComponent>(rightX, (int) (this.height*0.2f), rightW, (int) (this.height*0.7f),(x,y)->{
			if(subDrawer==null)
				return true;
			return !subDrawer.isMouseOver(x,y);
		}));
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Valider"),(b)->exportSkin()).dimensions( rightX,(int) (this.height*0.9f)+1,rightW,20).build());
		scroll.setGridSize(8,8);
		scroll.setSplittedListner(b->{
				splittedState.computeIfAbsent(currentLayer,s->new HashMap<>()).put(currentIndex,b);
				var l=getLayerPair(currentIndex);
				if(l.getLeft()!=null&& CharactersApi.bodyParts().getBodyLayer(currentLayer).get().splited()) {
					var n = new SkinLayerValue();
					n.layer = l.getLeft().layer;
					n.side = "right";
					n.parent = l.getLeft().parent;
					n.id = l.getLeft().id;
					n.category = l.getLeft().category;
					l.setRight(n);
				}
				else
					l.setRight(null);
				playerComp.getRenderer().setPart(l.getLeft(),layersLinkage.get(currentLayer).get(currentIndex));
				playerComp.getRenderer().setPart(l.getRight(),layersLinkage.get(currentLayer).get(currentIndex));
				scroll.components.forEach(c1-> c1.isEquipped=c1.isSelectedFn.apply(c1.getPart()));
		});
		layerListComp.setListener(new LayerList.LayerSelectEvent() {
			@Override
			public void onLayerAdded(int layerIndex) {
				var map=layersLinkage.computeIfAbsent(currentLayer,k->new HashMap<>());
				var ind=playerComp.getRenderer().addLayer(CharactersApi.bodyParts().getBodyLayer(currentLayer).get());
				map.put(layerIndex,ind);
				map.put(-1,map.get(-1)+1);
			}

			@Override
			public void onLayerRemoved(int layerIndex) {
				var map=layersLinkage.get(currentLayer);
				if(map!=null)
				{
					playerComp.getRenderer().removeLayer(map.get(layerIndex));
					layers.get(currentLayer).remove(layerIndex);
					map.put(-1,Math.max(map.get(-1)-1,0));
				}
			}

			@Override
			public void onLayerSelected(int layerIndex) {
				loadCloths(scroll, playerComp, lastSearch,layerIndex);
			}

		});
		var search=new TextFieldWidget(client.textRenderer,rightX, (int) (this.height*0.2f)-20,rightW,18,Text.translatable(Characters.MOD_ID+".wardrobe.search"));
		var slider= new SliderWidget(0,0,0,20,Text.literal("0°"),0.5f) {
			@Override
			protected void updateMessage() {
				int v= (int) (-180+(this.value*360f));
				this.setMessage(Text.literal(v+"°"));
			}

			@Override
			protected void applyValue() {
				playerComp.rotation((int) (-180 + (this.value * 360f)));
			}
		};

		List<Pair<SkinLayerValue,SkinLayerValue>> ls1=new ArrayList<>();
		layers.values().forEach((a)->ls1.addAll(a.values()));
		if(layers.isEmpty())
			ls1.addAll(layerArrayConverter(equippedProvider.apply(MinecraftClient.getInstance().player)));
		layers.clear();

		playerComp.getRenderer().init(ls1,(l,i)->{
			var map=layersLinkage.computeIfAbsent(l.getLeft().layer,k->new HashMap<>());
			var cur=map.getOrDefault(-1,0);
			map.put(cur,i);
			var map1=layers.computeIfAbsent(l.getLeft().layer,k->new HashMap<>());
			map1.put(cur,l);
			map.put(-1,cur+1);
		});

		search.setPlaceholder(Text.translatable(Characters.MOD_ID+".wardrobe.search"));
		addSelectableChild(slider);
		addDrawable(slider);
		addSelectableChild(search);
		addDrawable(search);
		if(subDrawer!=null) {
			addDrawableChild(subDrawer);
		}
		components.forEach(c->c.mount(0,0,this.width,this.height));
		playerComp.x= (int) (width*0.15f);
		playerComp.y=(this.height/2)- playerComp.height/2;
		slider.setX(playerComp.x+20);
		slider.setWidth(playerComp.width-40);
		slider.setY(playerComp.y+playerComp.height+5);
		var player=playerComp.entity();
		var dt=ComponentManager.getPlayerDatas(player);
		dt.getAppearence().clonePlayerAppearance(MinecraftClient.getInstance().player);

		loadCloths(scroll, playerComp, "",0);
		search.setChangedListener(v -> loadCloths(scroll, playerComp, v.toLowerCase(),layerListComp.getSelected()));
		this.setFocused(search);
			var outfits = dt.getAppearence().getSavedLayersLabels(this.saveId);
			int mid = (int) ((this.height * 0.2f) - 20);
			for (int i = 1; i <= 7; i++) {
				var v = Text.translatable(Characters.MOD_ID + ".wardrobe.outfitbt", i);
				final var i1 = i - 1;
				if (i1 < outfits.size())
					v = Text.literal(outfits.get(i1).getLeft());
				var bt = new ButtonWidget.Builder(v.copy(), (a) -> {
					if (Screen.hasShiftDown()) {
						createOutfitWindow(a, i1);
					} else {

						var layers=dt.getAppearence().getSavedLayers(this.saveId,i1);
						if(layers !=null && layers.length>0)
						{
						MinecraftClient.getInstance().setScreen(new BodyPartEditorGui(layers,this.saveId,this.allowedLayers));
						}

					}
				}).position(5, mid + (22 * (i - 1))).size((int) (this.width * 0.2f), 20)
						.tooltip(Tooltip.of(v.append(Text.translatable(Characters.MOD_ID + ".wardrobe.outfitbt.tooltip").formatted(Formatting.GRAY, Formatting.ITALIC)))).build();
				addDrawableChild(bt);
				addSelectableChild(bt);
			}
			final List<CategoryButton> bts = new ArrayList<>();
		for (int i = 0; i < layerBts.size(); i++) {
			BodyLayerInfo value =layerBts.get(i);
			var bt = new CategoryButton(Text.empty(),Characters.asRessource("textures/gui/icons/body/"+value.id()+".png"), button -> {
				for (var d : bts) {
					d.setActivated(false);
				}
				if(button instanceof CategoryButton oc)
					oc.setActivated(true);
				currentLayer = value.id();
				layerListComp.setState(0,layersLinkage.computeIfAbsent(currentLayer,s->new HashMap<>()).getOrDefault(-1,1));
				CharactersApi.bodyParts().getBodyLayer(currentLayer).ifPresent(l->{
					layerListComp.visible=l.multi();
				});
				updateDrawer(null);
				loadCloths(scroll, playerComp, lastSearch,0);
			});
			bt.setX((int) ((this.width*0.43f)+(i%2==0?0:22)));
			bt.setY(50+(22*(i/2)));
			bt.setTooltip(Tooltip.of(Text.translatable(Characters.MOD_ID + ".body_editor.layerbt." + value.id())));
			addSelectableChild(bt);
			addDrawable(bt);
			bts.add(bt);
		}
		layerListComp.setState(0,layersLinkage.computeIfAbsent(currentLayer,s->new HashMap<>()).getOrDefault(-1,1));
		CharactersApi.bodyParts().getBodyLayer(currentLayer).ifPresent(l->{
			layerListComp.visible=l.multi();
		});
	}
	private boolean isButtonSelected(BodyPartButtonComponent.BodyPartRef ref, @Nullable String side)
	{
		var ls=layers.get(ref.layer());
		if(ls!=null)
		{
			for(Map.Entry<Integer, Integer> entry : layersLinkage.get(ref.layer()).entrySet()) {

				if(entry.getKey()==currentIndex)
				{

					var pair=ls.get(entry.getKey());
					if(pair ==null)
						return false;
					var v="right".equals(side)?pair.getRight():pair.getLeft();
					if(v==null)
						return false;
					if(ref.image().hasSubs())
					{
						return ref.image().id().equals(v.parent);
					}
					else
					{
						if(v.parent==null)
							return ref.image().id().equals(v.id);
						else if(ref.parent() !=null)
						{
							return v.parent.equals(ref.parent().id()) &&ref.image().id().equals(v.id);
						}

					}
					return false;
				}
			}
		}
		return false;
	}
	private static List<BodyPartButtonComponent.BodyPartRef> convertList(String layer, List<Pair<BodyLayerCategory, List<BodyLayerImageGroup>>> list)
	{
		List<BodyPartButtonComponent.BodyPartRef> ls=new ArrayList<>();
		CharactersApi.bodyParts().getBodyLayer(layer).ifPresent(l->{
			if(l.clearable()){

				ls.add(new BodyPartButtonComponent.BodyPartRef(new BodyLayerImageGroup("clear",null),l.id(),null,null));
			}
		});
		list.forEach(p->{
			var cat=p.getLeft().id().equals("default")?null:p.getLeft().id();
			ls.addAll(p.getRight().stream().map(l->new BodyPartButtonComponent.BodyPartRef(l,layer,null,cat)).toList());
		});
		return ls;
	}
	private Pair<SkinLayerValue, SkinLayerValue> getLayerPair(int key)
	{
		return layers.computeIfAbsent(currentLayer,k->new HashMap<>()).computeIfAbsent(key,k->new Pair<>(null,null));
	}

	private void loadCloths(ScrollableBodyParts<BodyPartButtonComponent.BodyPartRef,BodyPartButtonComponent> layout, LayeredPlayerComponent playerComp, String filter,int selectedIndex) {
		lastSearch = filter;
		currentIndex=selectedIndex;
		CharactersApi.bodyParts().getBodyLayer(currentLayer).ifPresent(layerInfo->{
		CharactersApi.bodyParts().getBodyParts(currentLayer).ifPresent(dictionary->{
		var list=dictionary.filterImageGroups(filter);
			layout.setCloths(convertList(dictionary.id(),list),(c,side)->{
				var bt = new BodyPartButtonComponent(c,
						( a) -> {
							a.playDownSound(MinecraftClient.getInstance().getSoundManager());
							/*if (oldCloths.get(c.layer().id)==c) {
								dt.getAppearence().setCloth(c.layer().id, null);
								Channels.MAIN.clientHandle().send(new CosmeticsPackets.EquipCloth(new Identifier("null","null"), c.layer().getId()));
							} else {
								dt.getAppearence().setCloth(c.layer().id, c);
								Channels.MAIN.clientHandle().send(new CosmeticsPackets.EquipCloth(c.id(), c.layer().toString()));
							}
							oldCloths =  equippedProvider.apply(player);
							updateSelected(layout, oldCloths.values().stream().filter(Objects::nonNull).toList());

							 */
							var v=((BodyPartButtonComponent)a).getPart();
							if(!v.image().hasSubs()) {
								var l = new SkinLayerValue();
								l.layer = v.layer();
								if(v.parent() != null)
									l.parent = v.parent().id();
								l.category = v.category();
								l.id = v.image().id();
								l.side = side;
								playerComp.getRenderer().setPart(l, layersLinkage.computeIfAbsent(currentLayer, k -> new HashMap<>()).getOrDefault(selectedIndex,0));
								((BodyPartButtonComponent)a).isEquipped=true;
								var sp=CharactersApi.bodyParts().getBodyLayer(currentLayer).get().splited();
								if("right".equals(side) && sp)
									getLayerPair(selectedIndex).setRight(l);
								else
									getLayerPair(selectedIndex).setLeft(l);

								layout.components.forEach(c1-> c1.isEquipped=c1.isSelectedFn.apply(c1.getPart()));

							}
						},layout,this::updateDrawer,()->this.subDrawer,(f)->this.isButtonSelected(f,side));
				bt.onPartHovered((v) -> {
					if(v!=null && !v.image().hasSubs()) {
						var l=new SkinLayerValue();
						l.layer=v.layer();
						if(v.parent()!=null)
							l.parent=v.parent().id();
						l.category=v.category();
						l.id=v.image().id();
						l.side=side;
						playerComp.getRenderer().setPart(l,layersLinkage.computeIfAbsent(currentLayer,k->new HashMap<>()).getOrDefault(selectedIndex,0));
					}
					else
					{
						var l=getLayerPair(selectedIndex);
						if(l!=null) {
							var v1 = l.getLeft();
							if("right".equals(side) && l.getRight() != null)
								v1 = l.getRight();
							if(v1!=null)
								playerComp.getRenderer().setPart(v1, layersLinkage.computeIfAbsent(currentLayer, k -> new HashMap<>()).getOrDefault(selectedIndex, 0));
						}
					}


				});
				return bt;
			},layerInfo.splited(),splittedState.computeIfAbsent(currentLayer,s->new HashMap<>()).getOrDefault(selectedIndex,true));
		});

		//	updateSelected(layout, equip);
		});

	}

	/*private void updateSelected(ScrollableCloths<BodyPartButtonComponent.BodyPartRef,BodyPartButtonComponent> layout, List<Cloth> equipped) {
		for (BodyPartButtonComponent cb : layout.children()) {
				cb.setSelected(equipped.stream().anyMatch(v -> v.id().equals(cb.getCloth().id())));
			}
	}
*/
	private void createOutfitWindow(ButtonWidget bt, int index) {
		MinecraftClient.getInstance().setScreen(new LayerPopupGui(this.allowedLayers,this.saveId, index, flattenLayers()));
	}

	public boolean shouldPause() {
		return false;
	}
}
