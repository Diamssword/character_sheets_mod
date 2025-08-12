package com.diamssword.characters.api;

public abstract class CharactersApi {
	 public static CharactersApi instance;
	abstract public void addCloth(Cloth cloth);
	abstract public void getCloths();
}
