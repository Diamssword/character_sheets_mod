package com.diamssword.characters.api.stats;

import com.diamssword.characters.api.stats.StatsRole;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsRolePalier {

	public final int level;
	public final StatsRole parent;
	private final Map<EntityAttribute, EntityAttributeModifier> modifiers = new HashMap<>();

	public StatsRolePalier(StatsRole parent, int level) {
		this.level = level;
		this.parent = parent;
	}

	public Map<EntityAttribute, EntityAttributeModifier> getModifiers() {
		return modifiers;
	}

	public void addModifier(EntityAttribute attribute, EntityAttributeModifier modifier) {
		modifiers.put(attribute, modifier);
	}

	public void changeModifiers(PlayerEntity pl) {
		for (var set : modifiers.entrySet()) {
			var max = pl.getAttributeInstance(set.getKey());
			if (max != null) {
				max.tryRemoveModifier(set.getValue().getId());
				max.addPersistentModifier(set.getValue());
			}
		}
	}

	public void clearModifier(PlayerEntity pl) {
		for (var set : modifiers.entrySet()) {
			var max = pl.getAttributeInstance(set.getKey());
			if (max != null)
				max.tryRemoveModifier(set.getValue().getId());
		}
	}
}
