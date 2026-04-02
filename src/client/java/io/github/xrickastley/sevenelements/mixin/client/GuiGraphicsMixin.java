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

import net.minecraft.client.gui.GuiGraphics.ScissorStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements ExtendedDrawContext {
	@Shadow
	@Final
   	public ScissorStack scissorStack;

	@Shadow
   	@Final
	public GuiRenderState guiRenderState;

	@Shadow
	@Final
	private Matrix3x2fStack pose;

	@Override
	@Unique
	public void sevenelements$drawCircle(RenderPipeline pipeline, float x, float y, float radius, int color) {
		this.guiRenderState.submitGuiElement(
			new CircleGuiElementRenderState(pipeline, new Matrix3x2f(this.pose), x, y, radius, color, this.scissorStack.peek())
		);
	}
}
