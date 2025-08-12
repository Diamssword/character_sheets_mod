package com.diamssword.characters.fabric.client.gui.components;

import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ComponentsRegister {
	public static void init() {
		MinecraftClient mc = MinecraftClient.getInstance();

		UIParsing.registerFactory("player", PlayerComponent::parse);

		UIParsing.registerFactory("separator", SeparatorComponent::parse);
		UIParsing.registerFactory("rbutton", (a) -> new RButtonComponent(Text.empty(), (RButtonComponent button) -> {
		}));
		UIParsing.registerFactory("freerowgrid", FreeRowGridLayout::parse);


	}
}
