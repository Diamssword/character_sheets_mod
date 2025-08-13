package com.diamssword.characters.fabric.client.gui;

import com.diamssword.characters.Characters;
import com.diamssword.characters.storage.ClothingLoader;
import com.diamssword.characters.api.Cloth;
import com.diamssword.characters.api.LayerDef;
import com.diamssword.characters.fabric.client.gui.components.ClothButtonComponent;
import com.diamssword.characters.fabric.client.gui.components.FreeRowGridLayout;
import com.diamssword.characters.fabric.client.gui.components.PlayerComponent;
import com.diamssword.characters.fabric.client.gui.components.RButtonComponent;
import com.diamssword.characters.network.Channels;
import com.diamssword.characters.network.packets.CosmeticsPackets;
import com.diamssword.characters.api.ComponentManager;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SlimSliderComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WardrobeGui extends BaseUIModelScreen<FlowLayout> {

	private static final List<Pair<String, LayerDef[]>> layerBts = new ArrayList<>();

	static {
		var ls= ClothingLoader.instance.getClothLayers();
		layerBts.add(new Pair<>("all", ls.toArray(new LayerDef[0])));
		layerBts.add(new Pair<>("current", ls.toArray(new LayerDef[0])));
		for (var l : ls) {
			layerBts.add(new Pair<>(l.getId(), new LayerDef[]{l}));
		}
	}

	private Pair<String, LayerDef[]> currentLayer = layerBts.get(0);
	private Map<String, Cloth> oldCloths;

	private String lastSearch = "";
	private final String currentCol = "all";

	public WardrobeGui() {
		super(FlowLayout.class, DataSource.asset(Characters.asRessource("wardrobe")));
	}

	private void loadCloths(FreeRowGridLayout layout, PlayerComponent playerComp, String filter) {
		lastSearch = filter;
		var player = playerComp.entity();
		var dt = ComponentManager.getPlayerDatas(player);
		oldCloths = dt.getAppearence().getEquippedCloths();
		var equip = oldCloths.values().stream().filter(Objects::nonNull).toList();
		List<Cloth> list;
		if (currentLayer.getLeft().equals("current"))
			list = equip;
		else
			list = ClothingLoader.instance.getAvailablesClothsCollectionForPlayer(MinecraftClient.getInstance().player, "all", currentLayer.getRight());
		if (!filter.isEmpty())
			list = list.stream().filter(v -> v.name().toLowerCase().contains(filter)).toList();
		layout.clear();
		for (var c : list) {
			var bt = new ClothButtonComponent(c);
			bt.onPress((__) -> {
				var v = bt.getCloth();
				if (oldCloths.containsValue(v)) {
					dt.getAppearence().setCloth(v.layer().id, null);
					Channels.MAIN.clientHandle().send(new CosmeticsPackets.EquipCloth("null", v.layer().getId()));
				} else {
					dt.getAppearence().setCloth(v.layer().id, v);
					Channels.MAIN.clientHandle().send(new CosmeticsPackets.EquipCloth(v.layer().getId() + "_" + v.id(), v.layer().toString()));
				}
				oldCloths = dt.getAppearence().getEquippedCloths();
				updateSelected(layout, oldCloths.values().stream().filter(Objects::nonNull).toList());

			});
			bt.onClothHovered().subscribe(v -> {
				if (v != null)
					dt.getAppearence().setCloth(v);
				else {
					oldCloths.forEach((a, v1) -> {
						dt.getAppearence().setCloth(a, v1);
					});
				}
			});
			layout.child(bt.sizing(Sizing.fixed(30), Sizing.fixed(50)).margins(Insets.of(1)));
		}
		updateSelected(layout, equip);
	}

	private void updateSelected(FreeRowGridLayout layout, List<Cloth> equipped) {
		for (Component child : layout.children()) {
			if (child instanceof ClothButtonComponent cb) {
				cb.setSelected(equipped.stream().anyMatch(v -> v.id().equals(cb.getCloth().id())));
			}
		}
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		//   var ward=rootComponent.childById(ClothInventoryComponent.class,"cloths");
		var wardLay = rootComponent.childById(FreeRowGridLayout.class, "clothContainer");
		var search = rootComponent.childById(TextBoxComponent.class, "search");
		var flow = rootComponent.childById(FreeRowGridLayout.class, "layerLayout");
		var txt1 = rootComponent.childById(LabelComponent.class, "title_right");
		var playerComp = rootComponent.childById(PlayerComponent.class, "player");
		var slider = rootComponent.childById(SlimSliderComponent.class, "slider");
		var player = playerComp.entity();
		var cp = new NbtCompound();

		//MinecraftClient.getInstance().player.getComponent(Components.PLAYER_DATA).writeToNbt(cp);
		var dt=ComponentManager.getPlayerDatas(player);
		//var dt = player.getComponent(Components.PLAYER_DATA);
		//dt.readFromNbt(cp);
		dt.getAppearence().clonePlayerAppearance(MinecraftClient.getInstance().player);

		loadCloths(wardLay, playerComp, "");
		search.onChanged().subscribe(v -> loadCloths(wardLay, playerComp, v.toLowerCase()));
		search.setPlaceholder(Text.literal("Recherche"));
		this.setFocused(search);
		wardLay.focusGained().subscribe(v -> {
			this.setFocused(search);
		});
		var outfits = dt.getAppearence().getOutfits();
		slider.value(0.5);
		slider.onChanged().subscribe(v -> {
			playerComp.rotation((int) (-180 + (v * 360f)));
		});
		for (int i = 1; i <= 7; i++) {
			var v = Text.literal("Outfit " + i);
			final var i1 = i - 1;
			if (i1 < outfits.size())
				v = Text.literal(outfits.get(i1).getLeft());
			var ar = new ArrayList<Text>();
			ar.add(v);
			ar.add(Text.literal("[maj] + [clique] pour modifier cette tenue").formatted(Formatting.GRAY, Formatting.ITALIC));
			rootComponent.childById(RButtonComponent.class, "memory" + i).onPress(v1 -> {
				if (Screen.hasShiftDown()) {
					createOutfitWindow(v1, i1);
				} else {
					Channels.MAIN.clientHandle().send(new CosmeticsPackets.EquipOutfit(i1));
					dt.getAppearence().equipOutfit(i1);
				}
			}).tooltip(ar);
		}
		if (flow != null) {
			final List<RButtonComponent> bts = new ArrayList<>();
			for (Pair<String, LayerDef[]> value : layerBts) {
				var bt = new RButtonComponent(Text.empty(), (o) -> {
					for (var d : bts) {
						d.setActivated(false);
					}
					o.setActivated(true);
					currentLayer = value;
					txt1.text(Text.translatable(Characters.MOD_ID + ".wardrobe.collection." + currentLayer.getLeft()));
					loadCloths(wardLay, playerComp, lastSearch);
				});
				if (value.getLeft().equals("all"))
					bt.setActivated(true);
				bt.icon(value.getLeft()).sizing(Sizing.fixed(20)).tooltip(Text.translatable(Characters.MOD_ID + ".wardrobe.layerbt." + value.getLeft())).margins(Insets.of(2, 0, 2, 0));
				flow.child(bt);
				bts.add(bt);

			}

		}

	}

	private void createOutfitWindow(RButtonComponent bt, int index) {
		MinecraftClient.getInstance().setScreen(new OutfitPopupGui(this, index, bt));
	}

	public boolean shouldPause() {
		return false;
	}
}