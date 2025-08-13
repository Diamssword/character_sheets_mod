package com.diamssword.characters.api.http;

import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;

public class ApiStatsValues {
	public String firstname;
	public String lastname;
	public int age;
	public String faction;
	public String origine;
	public String job;
	public Map<String, Integer> points;

	public NbtCompound toNBT() {
		var tag = new NbtCompound();
		tag.putString("firstname", firstname);
		tag.putString("lastname", lastname);
		tag.putString("faction", faction);
		tag.putString("origine", origine);
		tag.putString("job", job);
		tag.putInt("age", age);
		var pts = new NbtCompound();
		points.forEach(pts::putInt);
		tag.put("points", pts);
		return tag;
	}

	public ApiStatsValues fromNBT(NbtCompound tag) {
		firstname = tag.getString("firstname");
		lastname = tag.getString("lastname");
		faction = tag.getString("faction");
		origine = tag.getString("origine");
		job = tag.getString("job");
		age = Math.max(tag.getInt("age"), 15);
		var pts = tag.getCompound("points");
		points = new HashMap<>();
		pts.getKeys().forEach(k -> points.put(k, tag.getInt(k)));
		return this;
	}

}
