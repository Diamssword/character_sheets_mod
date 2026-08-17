package com.diamssword.characters.api;

import com.diamssword.characters.api.appearence.LayerDef;
import com.diamssword.characters.api.clothing.ClothData;
import com.diamssword.characters.api.http.ApiSkinValues;

import java.util.Optional;

public interface IPlayerAppearanceProvider {
	public ApiSkinValues getSkinDatas();
	public Optional<ClothData> getClothDatas(LayerDef layer);

}
