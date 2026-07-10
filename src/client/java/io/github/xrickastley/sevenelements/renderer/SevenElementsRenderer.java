package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.mixin.client.BufferBuilderAccessor;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public abstract class SevenElementsRenderer<S> {
	private static final List<BufferAllocator> ALLOCATORS = new ArrayList<>();
	private static final Map<Class<? extends SevenElementsRenderer<?>>, SevenElementsRenderer<?>> SINGLETON_MAP = new HashMap<>();

	/**
	 * Whether the render states are cleared upon rendering.
	 *
	 * <p>Render states are cleared <b>before</b> {@link #afterRender()} is called.
	 */
	protected static final int CLEAR_RENDER_STATES = 1 << 0;
	/**
	 * Whether the legacy render world matrix transforms done in previous Minecraft versions
	 * are done before rendering.
	 *
	 * <p>Legacy transforms are set <b>before</b> {@link #beforeRender()} is called, and are
	 * cleared before {@link #afterRender()} is called.
	 */
	protected static final int LEGACY_TRANSFORMS = 1 << 1;

	private final int bitflags;
	private final Map<RenderPipeline, Map<Integer, BufferAllocator>> pipelineAllocators = new HashMap<>();
	protected final List<S> states = new ArrayList<>();

	protected SevenElementsRenderer() {
		this(0);
	}

	@SuppressWarnings("unchecked")
	protected SevenElementsRenderer(int bitflags) {
		if (SevenElementsRenderer.SINGLETON_MAP.containsKey(this.getClass()))
			throw new IllegalStateException("The class: " + this.getClass().getSimpleName() + " must be a singleton!");

		SevenElementsRenderer.SINGLETON_MAP.put((Class<? extends SevenElementsRenderer<?>>) this.getClass(), this);

		this.bitflags = bitflags;
	}

	/**
	 * Adds a render state to this renderer.
	 *
	 * @param state The render state to add to this renderer.
	 */
	public final void addRenderState(S state) {
		this.states.add(state);
	}

	/**
	 * Adds multiple render states to this renderer.
	 *
	 * @param state The render states to add to this renderer.
	 */
	@SafeVarargs
	public final void addRenderStates(S... states) {
		Stream.of(states).forEach(this::addRenderState);
	}

	/**
	 * Adds multiple render states to this renderer.
	 *
	 * @param state The render states to add to this renderer.
	 */
	public final void addRenderStates(List<S> states) {
		this.states.addAll(states);
	}

	/**
	 * This method is called before each render state is rendered individually.
	 *
	 * <p>Use it to setup rendering objects such as the {@code MatrixStack} or to make calls to
	 * {@code RenderSystem}.
	 *
	 * @param context The current world rendering context.
	 */
	protected void beforeRender(WorldRenderContext context) {}

	protected final void render(WorldRenderContext context) {
		if (this.hasFlag(SevenElementsRenderer.LEGACY_TRANSFORMS)) {
			final MatrixStack matrices = context.matrixStack();
			final Camera camera = context.camera();

			matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
		}

		this.beforeRender(context);

		this.states.forEach(state -> this.render(context, state));

		if (this.hasFlag(SevenElementsRenderer.CLEAR_RENDER_STATES))
			this.states.clear();

		if (this.hasFlag(SevenElementsRenderer.LEGACY_TRANSFORMS))
			context.matrixStack().pop();

		this.afterRender(context);
	}

	/**
	 * Gets a {@code BufferBuilder} for the provided {@code RenderPipeline}.
	 *
	 * <p>If there is none, this method automatically creates a {@code BufferAllocator} instance
	 * for the returned {@code BufferBuilder}, which is automatically closed when the game is
	 * closed.
	 *
	 * <p>Do note that each instance of the {@code BufferBuilder} is backed by the same
	 * {@code BufferAllocator} instance, which may cause rendering issues when two default buffer
	 * builder instances are written to.
	 *
	 * <p>If you need a {@code BufferBuilder} with a <i>unique</i> {@code BufferAllocator} instance
	 * for the same {@code RenderPipeline}, use the second overload.
	 *
	 * @param pipeline The {@code RenderPipeline} to get a {@code BufferBuilder} for.
	 * @param bufferSize The size of the {@code BufferAllocator}, if none exists yet.
	 * @return A {@code BufferBuilder} for the provided {@code RenderPipeline}.
	 */
	protected final BufferBuilder getBuffer(RenderPipeline pipeline, int bufferSize) {
		return this.getBuffer(pipeline, 0, bufferSize);
	}

	/**
	 * Gets a {@code BufferBuilder} with the provided {@code id} for the provided
	 * {@code RenderPipeline}.
	 *
	 * <p>An {@code id} corresponds to a single, unique {@code BufferAllocator} instance, which can
	 * be used for rendering with multiple {@code BufferBuilder} instances.
	 *
	 * <p>If there is none, this method automatically creates a {@code BufferAllocator} instance
	 * for the returned {@code BufferBuilder}, which is automatically closed when the game is
	 * closed.
	 *
	 * @param pipeline The {@code RenderPipeline} to get a {@code BufferBuilder} for.
	 * @param id A unique integer id to refer to a single, unique {@code BufferAllocator} instance.
	 * @param bufferSize The size of the {@code BufferAllocator}, if none exists yet.
	 * @return A {@code BufferBuilder} using the provided {@code id} for the provided {@code RenderPipeline}.
	 */
	protected final BufferBuilder getBuffer(RenderPipeline pipeline, int id, int bufferSize) {
		return new BufferBuilder(
			this.pipelineAllocators
				.computeIfAbsent(pipeline, p-> new HashMap<>())
				.computeIfAbsent(id, i -> new BufferAllocator(bufferSize)),
			pipeline.getVertexFormatMode(),
			pipeline.getVertexFormat()
		);
	}

	/**
	 * This method is called per render state to be rendered individually.
	 *
	 * @param context The current world rendering context.
	 * @param state The render state to be rendered.
	 */
	protected abstract void render(WorldRenderContext context, S state);

	/**
	 * Build and draws the provided {@code buffer} in the world using the provided
	 * {@code RenderPipeline}.
	 *
	 * @param buffer The {@code BufferBuilder} to build and draw.
	 * @param pipeline The {@code RenderPipeline} to use.
	 */
	protected final void draw(BufferBuilder buffer, RenderPipeline pipeline) {
		this.draw(buffer, pipeline, Optional.empty());
	}

	/**
	 * Build and draws the provided {@code buffer} in the world using the provided
	 * {@code RenderPipeline}.
	 *
	 * <p>This also binds the provided {@code texture}, if existent, to {@code Sampler0}.
	 *
	 * @param buffer The {@code BufferBuilder} to build and draw.
	 * @param pipeline The {@code RenderPipeline} to use.
	 * @param texture The texture to bind to {@code Sampler0}, if existent.
	 */
	protected final void draw(BufferBuilder buffer, RenderPipeline pipeline, Optional<Identifier> texture) {
		final BuiltBuffer builtBuffer = buffer.end();
		final MinecraftClient client = MinecraftClient.getInstance();

		try {
			final GpuBuffer gpuBuffer = pipeline.getVertexFormat().uploadImmediateVertexBuffer(builtBuffer.getBuffer());
			GpuBuffer gpuBuffer2;
			VertexFormat.IndexType indexType;
			if (builtBuffer.getSortedBuffer() == null) {
				RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(builtBuffer.getDrawParameters().mode());
				gpuBuffer2 = shapeIndexBuffer.getIndexBuffer(builtBuffer.getDrawParameters().indexCount());
				indexType = shapeIndexBuffer.getIndexType();
			} else {
				gpuBuffer2 = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.getSortedBuffer());
				indexType = builtBuffer.getDrawParameters().indexType();
			}

			final Framebuffer framebuffer = client.getFramebuffer();

			try (
				RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(
						framebuffer.getColorAttachment(), OptionalInt.empty(), framebuffer.useDepthAttachment ? framebuffer.getDepthAttachment() : null, OptionalDouble.empty()
					)
			) {
				renderPass.setPipeline(pipeline);
				renderPass.setVertexBuffer(0, gpuBuffer);

				if (RenderSystem.SCISSOR_STATE.isEnabled())
					renderPass.enableScissor(RenderSystem.SCISSOR_STATE);

				if (texture.isPresent())
					renderPass.bindSampler("Sampler0", client.getTextureManager().getTexture(texture.get()).getGlTexture());

				renderPass.setIndexBuffer(gpuBuffer2, indexType);
				renderPass.drawIndexed(0, builtBuffer.getDrawParameters().indexCount());
			}
		} catch (Throwable renderError) {
			if (builtBuffer != null) {
				try {
					builtBuffer.close();
				} catch (Throwable bufferError) {
					renderError.addSuppressed(bufferError);
				}
			}

			throw renderError;
		}

		if (builtBuffer != null)
			builtBuffer.close();
	}

	/**
	 * This method is called after each render state is rendered individually.
	 *
	 * <p>Use it to reset rendering objects to their previous state, such as the
	 * {@code MatrixStack}.
	 *
	 * @param context The current world rendering context.
	 */
	protected void afterRender(WorldRenderContext context) {}

	/**
	 * This method is called at the start of each client tick.
	 *
	 * @param world The current client world.
	 */
	protected void tick(ClientWorld world) {}

	private boolean hasFlag(int bitflag) {
		return (this.bitflags & bitflag) != 0;
	}

	public static BufferBuilder createBuffer(final BufferAllocator allocator, final RenderPipeline pipeline) {
		return SevenElementsRenderer.createBuffer((BufferBuilder) null, allocator, pipeline);
	}

	public static BufferBuilder createBuffer(final @Nullable BufferBuilder buffer, final BufferAllocator allocator, final RenderPipeline pipeline) {
		return buffer == null || !((BufferBuilderAccessor) buffer).isBuilding()
			? new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat())
			: buffer;
	}

	public static BufferAllocator createAllocator(final Supplier<RenderLayer> layer) {
		return SevenElementsRenderer.createAllocator(layer.get());
	}

	public static BufferAllocator createAllocator(final RenderLayer layer) {
		return SevenElementsRenderer.createAllocator(layer.getExpectedBufferSize());
	}

	public static BufferAllocator createAllocator(final int size) {
		final BufferAllocator allocator = new BufferAllocator(size);

		SevenElementsRenderer.ALLOCATORS.add(allocator);

		return allocator;
	}

	public static void close() {
		SevenElementsRenderer.ALLOCATORS.forEach(BufferAllocator::close);
		SevenElementsRenderer.SINGLETON_MAP
			.values()
			.forEach(renderer ->
				renderer.pipelineAllocators
					.values()
					.forEach(allocators ->
						allocators
							.values()
							.forEach(BufferAllocator::close)
					)
			);
	}

	public static void renderAll(final WorldRenderContext context) {
		SevenElementsRenderer.SINGLETON_MAP
			.values()
			.forEach(renderer -> renderer.render(context));
	}

	public static void tickAll(final ClientWorld world) {
		if (!world.getTickManager().shouldTick())
			return;

		SevenElementsRenderer.SINGLETON_MAP
			.values()
			.forEach(renderer -> renderer.tick(world));
	}
}
