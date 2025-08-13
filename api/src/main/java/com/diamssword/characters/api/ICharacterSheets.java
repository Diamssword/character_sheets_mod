package com.diamssword.characters.api;

import com.diamssword.characters.api.http.ApiCharacterValues;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ICharacterSheets {
	public Set<String> getCharactersNames();

	public ApiCharacterValues getCurrentCharacter();

	public String getCurrentCharacterID();

	public void switchCharacter(String id);

	public void deleteCharacter(String id);

	public String addNewCharacter(ApiCharacterValues character);
	public void replaceCharacter(String id, ApiCharacterValues character);

	public void attachComponent(Identifier id, Supplier<ICharacterStored> instance, Supplier<NbtCompound> serializer, Consumer<NbtCompound> unserializer);
	public void unattachComponent(Identifier id);
}
