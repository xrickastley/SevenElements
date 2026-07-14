package io.github.xrickastley.sevenelements.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.StagedVertexBuffer;

@Mixin(StagedVertexBuffer.class)
public interface StagedVertexBufferAccessor {
	@Accessor("draws")
	public List<StagedVertexBuffer.Draw> getDraws();
}
