package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

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
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.github.xrickastley.sevenelements.mixin.client.BufferBuilderAccessor;
import io.github.xrickastley.sevenelements.util.Functions;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public abstract class SevenElementsRenderer<S> {
	private static final List<ByteBufferBuilder> ALLOCATORS = new ArrayList<>();
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
	private final Map<RenderPipeline, Map<Integer, ByteBufferBuilder>> pipelineAllocators = new HashMap<>();
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
	 * This method is called after all render states have been extracted.
	 *
	 * <p>Use it to extract render states from the world and append them for rendering via
	 * {@link SevenElementsRenderer#addRenderState} or other similar methods.
	 *
	 * <p>Note that a render state should have as little data as possible, and it <b>must only</b>
	 * have thread-safe objects.
	 *
	 * @param context The current world extraction context.
	 */
	protected void extract(LevelExtractionContext context) {}

	/**
	 * This method is called before each render state is rendered individually.
	 *
	 * <p>Use it to setup rendering objects such as the {@code MatrixStack} or to make calls to
	 * {@code RenderSystem}.
	 *
	 * @param context The current world rendering context.
	 */
	protected void beforeRender(LevelRenderContext context) {}

	protected final void render(LevelRenderContext context) {
		if (this.hasFlag(SevenElementsRenderer.LEGACY_TRANSFORMS)) {
			final PoseStack matrices = context.poseStack();
			final Camera camera = context.gameRenderer().getMainCamera();

			matrices.pushPose();
			matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot()));
			matrices.mulPose(Axis.YP.rotationDegrees(camera.yRot() + 180.0F));
		}

		this.beforeRender(context);

		this.states.forEach(state -> this.render(context, state));

		if (this.hasFlag(SevenElementsRenderer.CLEAR_RENDER_STATES))
			this.states.clear();

		if (this.hasFlag(SevenElementsRenderer.LEGACY_TRANSFORMS))
			context.poseStack().popPose();

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
				.computeIfAbsent(id, i -> new ByteBufferBuilder(bufferSize)),
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
	protected abstract void render(LevelRenderContext context, S state);

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
		final MeshData builtBuffer = buffer.buildOrThrow();
		final Minecraft client = Minecraft.getInstance();

		try {
			final GpuBuffer gpuBuffer = pipeline.getVertexFormat().uploadImmediateVertexBuffer(builtBuffer.vertexBuffer());
			GpuBuffer gpuBuffer2;
			VertexFormat.IndexType indexType;
			if (builtBuffer.indexBuffer() == null) {
				RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(builtBuffer.drawState().mode());
				gpuBuffer2 = shapeIndexBuffer.getBuffer(builtBuffer.drawState().indexCount());
				indexType = shapeIndexBuffer.type();
			} else {
				gpuBuffer2 = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
				indexType = builtBuffer.drawState().indexType();
			}

			final RenderTarget framebuffer = client.getMainRenderTarget();
			final GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
				.writeTransform(
					RenderSystem.getModelViewMatrix(),
					new Vector4f(1f, 1f, 1f, 1f),
					new Vector3f(),
					new Matrix4f()
				);

			try (
				RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(
						Functions.supplier("Draw for SevenElementsRenderer instance: " + this.getClass().getSimpleName() + " with RenderPipeline: " + pipeline.getLocation()), framebuffer.getColorTextureView(), OptionalInt.empty(), framebuffer.getDepthTextureView(), OptionalDouble.empty()
					)
			) {
				renderPass.setPipeline(pipeline);

				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setUniform("DynamicTransforms", dynamicTransforms);
				renderPass.setVertexBuffer(0, gpuBuffer);

				if (texture.isPresent())
					renderPass.bindTexture("Sampler0", client.getTextureManager().getTexture(texture.get()).getTextureView(), null);

				renderPass.setIndexBuffer(gpuBuffer2, indexType);
				renderPass.drawIndexed(0, 0, builtBuffer.drawState().indexCount(), 1);
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
	protected void afterRender(LevelRenderContext context) {}

	/**
	 * This method is called at the start of each client tick.
	 *
	 * @param world The current client world.
	 */
	protected void tick(ClientLevel world) {}

	private boolean hasFlag(int bitflag) {
		return (this.bitflags & bitflag) != 0;
	}

	public static BufferBuilder createBuffer(final ByteBufferBuilder allocator, final RenderPipeline pipeline) {
		return SevenElementsRenderer.createBuffer((BufferBuilder) null, allocator, pipeline);
	}

	public static BufferBuilder createBuffer(final @Nullable BufferBuilder buffer, final ByteBufferBuilder allocator, final RenderPipeline pipeline) {
		return buffer == null || !((BufferBuilderAccessor) buffer).isBuilding()
			? new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat())
			: buffer;
	}

	public static ByteBufferBuilder createAllocator(final Supplier<RenderType> layer) {
		return SevenElementsRenderer.createAllocator(layer.get());
	}

	public static ByteBufferBuilder createAllocator(final RenderType layer) {
		return SevenElementsRenderer.createAllocator(layer.bufferSize());
	}

	public static ByteBufferBuilder createAllocator(final int size) {
		final ByteBufferBuilder allocator = new ByteBufferBuilder(size);

		SevenElementsRenderer.ALLOCATORS.add(allocator);

		return allocator;
	}

	public static void close() {
		SevenElementsRenderer.ALLOCATORS.forEach(ByteBufferBuilder::close);
		SevenElementsRenderer.SINGLETON_MAP
			.values()
			.forEach(renderer ->
				renderer.pipelineAllocators
					.values()
					.forEach(allocators ->
						allocators
							.values()
							.forEach(ByteBufferBuilder::close)
					)
			);
	}

	public static void extractAll(final LevelExtractionContext context) {
		SevenElementsRenderer.SINGLETON_MAP
			.values()
			.forEach(renderer -> renderer.extract(context));
	}

	public static void renderAll(final LevelRenderContext context) {
		SevenElementsRenderer.SINGLETON_MAP
			.values()
			.forEach(renderer -> renderer.render(context));
	}

	public static void tickAll(final ClientLevel world) {
		if (!world.tickRateManager().runsNormally())
			return;

		SevenElementsRenderer.SINGLETON_MAP
			.values()
			.forEach(renderer -> renderer.tick(world));
	}
}
