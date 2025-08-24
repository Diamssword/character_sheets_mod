package com.diamssword.characters.client.gui;

import com.diamssword.characters.Characters;
import com.diamssword.characters.api.ComponentManager;
import com.diamssword.characters.api.appearence.Cloth;
import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.client.gui.components.*;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.storage.ClothingLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class WardrobeGui extends Screen {
	private final List<BaseComponent> components=new ArrayList<>();
	private final List<Pair<String, LayerDef[]>> layerBts = new ArrayList<>();

	private Pair<String, LayerDef[]> currentLayer;
	private Map<String, Cloth> oldCloths;

	private String lastSearch = "";
	private final boolean shouldShowOutifits;
	private final Function<PlayerEntity,Map<String,Cloth>> equippedProvider;
	public WardrobeGui(String type) {
		super(Text.literal(""));
		List<LayerDef> layers;
		if(type ==null || type.equals("default")) {
			shouldShowOutifits=true;
			layers = ClothingLoader.instance.getClothLayers();
			layerBts.add(new Pair<>("all", layers.toArray(new LayerDef[0])));
			layerBts.add(new Pair<>("current", layers.toArray(new LayerDef[0])));
			equippedProvider=(p)->ComponentManager.getPlayerDatas(p).getAppearence().getEquippedCloths();
		}
		else {
			shouldShowOutifits=false;
			layers = ClothingLoader.instance.getLayers().values().stream().filter(v -> type.equals(v.getSpecialEditor())).toList();
			equippedProvider=(p)-> ComponentManager.getPlayerDatas(p).getAppearence().getEquippedLayers();
		}
		for (var l : layers) {
			layerBts.add(new Pair<>(l.getId(), new LayerDef[]{l}));
		}
		currentLayer=layerBts.get(0);
	}

	public <T extends BaseComponent> T addComponent(T comp)
	{
		this.components.add(comp);
		addDrawable(comp);
		return comp;
	}
	@Override
	protected void init() {
		super.init();
		this.components.clear();
		var playerComp =addComponent(new PlayerComponent(Sizing.fill(40),Sizing.content(0))).scaleToFit(true).lookAtCursor(true);
		int rightX=(int) (this.width*0.55f);
		int rightW=(int) (this.width*0.4f);
		var scroll=this.addDrawableChild(new ScrollableCloths(rightX, (int) (this.height*0.2f), rightW, (int) (this.height*0.7f)));
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
		search.setPlaceholder(Text.translatable(Characters.MOD_ID+".wardrobe.search"));
		addSelectableChild(slider);
		addDrawable(slider);
		addSelectableChild(search);
		addDrawable(search);
		components.forEach(c->c.mount(0,0,this.width,this.height));
		playerComp.x= (int) (width*0.15f);
		playerComp.y=(this.height/2)- playerComp.height/2;
		slider.setX(playerComp.x+20);
		slider.setWidth(playerComp.width-40);
		slider.setY(playerComp.y+playerComp.height+5);
		var player=playerComp.entity();
		var dt=ComponentManager.getPlayerDatas(player);
		dt.getAppearence().clonePlayerAppearance(MinecraftClient.getInstance().player);

		loadCloths(scroll, playerComp, "");
		search.setChangedListener(v -> loadCloths(scroll, playerComp, v.toLowerCase()));
		this.setFocused(search);
		if(shouldShowOutifits) {
			var outfits = dt.getAppearence().getOutfits();
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
						Channels.MAIN.clientHandle().send(new CosmeticsPackets.EquipOutfit(i1));
						dt.getAppearence().equipOutfit(i1);
						loadCloths(scroll, playerComp, lastSearch);
					}
				}).position(5, mid + (22 * (i - 1))).size((int) (this.width * 0.2f), 20)
						.tooltip(Tooltip.of(v.append(Text.translatable(Characters.MOD_ID + ".wardrobe.outfitbt.tooltip").formatted(Formatting.GRAY, Formatting.ITALIC)))).build();
				addDrawableChild(bt);
				addSelectableChild(bt);
			}
		}
			final List<CategoryButton> bts = new ArrayList<>();
		for (int i = 0; i < layerBts.size(); i++) {
			Pair<String, LayerDef[]> value =layerBts.get(i);
			var bt = new CategoryButton(Text.empty(),Characters.asRessource("textures/gui/icons/"+value.getLeft()+".png"), new ButtonWidget.PressAction() {
				@Override
				public void onPress(ButtonWidget button) {
					for (var d : bts) {
						d.setActivated(false);
					}
					if(button instanceof CategoryButton oc)
						oc.setActivated(true);
					currentLayer = value;
					//txt1.text(Text.translatable(Characters.MOD_ID + ".wardrobe.collection." + currentLayer.getLeft()));
					loadCloths(scroll, playerComp, lastSearch);
				}
			});
			bt.setX((int) ((this.width*0.45f)+(i%2==0?0:22)));
			bt.setY(50+(22*(i/2)));
			bt.setTooltip(Tooltip.of(Text.translatable(Characters.MOD_ID + ".wardrobe.layerbt." + value.getLeft())));
			if (value.getLeft().equals("all"))
				bt.setActivated(true);

			addSelectableChild(bt);
			addDrawable(bt);
			bts.add(bt);
		}
	}
	private void loadCloths(ScrollableCloths layout, PlayerComponent playerComp, String filter) {
		lastSearch = filter;
		var player = playerComp.entity();
		var dt = ComponentManager.getPlayerDatas(player);
		oldCloths = equippedProvider.apply(player);
		var equip = oldCloths.values().stream().filter(Objects::nonNull).toList();
		List<Cloth> list;
		if (currentLayer.getLeft().equals("current"))
			list = equip;
		else
			list = ClothingLoader.instance.getAvailablesClothsCollectionForPlayer(MinecraftClient.getInstance().player, "all", currentLayer.getRight());
		if (!filter.isEmpty())
			list = list.stream().filter(v -> v.name().toLowerCase().contains(filter) || (!v.collection().equals("default") && v.collection().toLowerCase().contains(filter))).toList();
		layout.setCloths(list,c->{
			var bt = new ClothButtonComponent(c,
			(a) -> {
				a.playDownSound(MinecraftClient.getInstance().getSoundManager());
				if (oldCloths.get(c.layer().id)==c) {
					dt.getAppearence().setCloth(c.layer().id, null);
					Channels.MAIN.clientHandle().send(new CosmeticsPackets.EquipCloth("null", c.layer().getId()));
				} else {
					dt.getAppearence().setCloth(c.layer().id, c);
					Channels.MAIN.clientHandle().send(new CosmeticsPackets.EquipCloth(c.layer().getId() + "_" + c.id(), c.layer().toString()));
				}
				oldCloths =  equippedProvider.apply(player);
				updateSelected(layout, oldCloths.values().stream().filter(Objects::nonNull).toList());
			},layout);
			bt.onClothHovered(v -> {
				if (v != null)
					dt.getAppearence().setCloth(v);
				else {
					oldCloths.forEach((a, v1) -> {
						dt.getAppearence().setCloth(a, v1);
					});
				}
			});
			return bt;
		});
		updateSelected(layout, equip);
	}

	private void updateSelected(ScrollableCloths layout, List<Cloth> equipped) {
		for (ClothButtonComponent cb : layout.children()) {
				cb.setSelected(equipped.stream().anyMatch(v -> v.id().equals(cb.getCloth().id())));
			}
	}

	private void createOutfitWindow(ButtonWidget bt, int index) {
		MinecraftClient.getInstance().setScreen(new OutfitPopupGui(this, index, bt));
	}

	public boolean shouldPause() {
		return false;
	}
}
