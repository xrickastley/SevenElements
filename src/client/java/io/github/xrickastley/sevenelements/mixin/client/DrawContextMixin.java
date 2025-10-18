package io.github.xrickastley.sevenelements.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import io.github.xrickastley.sevenelements.gui.render.state.CircleGuiElementRenderState;
import io.github.xrickastley.sevenelements.interfaces.ExtendedDrawContext;

import net.minecraft.client.gui.DrawContext.ScissorStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;

@Mixin(DrawContext.class)
public class DrawContextMixin implements ExtendedDrawContext {
	@Shadow
	@Final
   	public ScissorStack scissorStack;

	@Shadow
   	@Final
	public GuiRenderState state;

	@Shadow
	@Final
	private Matrix3x2fStack matrices;

	@Override
	@Unique
	public void sevenelements$drawCircle(RenderPipeline pipeline, float x, float y, float radius, int color) {
		this.state.addSimpleElement(
			new CircleGuiElementRenderState(pipeline, new Matrix3x2f(this.matrices), x, y, radius, color, this.scissorStack.peekLast())
		);
	}
}
