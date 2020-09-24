package com.elikill58.negativity.api.potion;

public class PotionEffect implements Cloneable {
	
	private final PotionEffectType type;
	private final int duration, amplifier;
	
	public PotionEffect(PotionEffectType type) {
		this(type, Integer.MAX_VALUE, 0);
	}
	
	public PotionEffect(PotionEffectType type, int duration, int amplifier) {
		this.type = type;
		this.duration = duration;
		this.amplifier = amplifier;
	}

	public PotionEffectType getType() {
		return type;
	}

	public int getDuration() {
		return duration;
	}

	public int getAmplifier() {
		return amplifier;
	}
	
	@Override
	public PotionEffect clone() {
		try {
			return (PotionEffect) super.clone();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
