package io.github.xrickastley.sevenelements.particle;

import java.util.Locale;

import com.mojang.serialization.MapCodec;

import io.github.xrickastley.sevenelements.util.dynamic.SevenElementsCodecs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class EntityEffectParticleEffect implements ParticleEffect {
	private final ParticleType<EntityEffectParticleEffect> type;
	private final int color;

	public static MapCodec<EntityEffectParticleEffect> createCodec(ParticleType<EntityEffectParticleEffect> type) {
		return SevenElementsCodecs.ARGB
			.xmap(color -> new EntityEffectParticleEffect(type, color), effect -> effect.color)
			.fieldOf("color");
	}

	private EntityEffectParticleEffect(ParticleType<EntityEffectParticleEffect> type, int color) {
		this.type = type;
		this.color = color;
	}

	public void write(PacketByteBuf buf) {
		buf.writeVarInt(this.color);
	}

	public String asString() {
		return String.format(Locale.ROOT, "%s %d", Registries.PARTICLE_TYPE.getId(this.getType()), this.color);
	}

	@Override
	public ParticleType<EntityEffectParticleEffect> getType() {
		return this.type;
	}

	public float getRed() {
		return ColorHelper.Argb.getRed(this.color) / 255.0F;
	}

	public float getGreen() {
		return ColorHelper.Argb.getGreen(this.color) / 255.0F;
	}

	public float getBlue() {
		return ColorHelper.Argb.getBlue(this.color) / 255.0F;
	}

	public float getAlpha() {
		return ColorHelper.Argb.getAlpha(this.color) / 255.0F;
	}

	public static EntityEffectParticleEffect create(ParticleType<EntityEffectParticleEffect> type, int color) {
		return new EntityEffectParticleEffect(type, color);
	}

	public static EntityEffectParticleEffect create(ParticleType<EntityEffectParticleEffect> type, float r, float g, float b) {
		return create(type, fromFloats(1.0F, r, g, b));
	}

	public static int fromFloats(float a, float r, float g, float b) {
		return ColorHelper.Argb.getArgb(channelFromFloat(a), channelFromFloat(r), channelFromFloat(g), channelFromFloat(b));
	}

	private static int channelFromFloat(float value) {
		return MathHelper.floor(value * 255.0F);
	}
}
