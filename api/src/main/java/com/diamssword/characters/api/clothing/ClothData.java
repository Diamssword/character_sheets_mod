package com.diamssword.characters.api.clothing;

import net.minecraft.util.Identifier;

public record ClothData(Identifier texture, boolean needColor, int color) {}