package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.github.xrickastley.sevenelements.mixin.client.BufferBuilderAccessor;
import io.github.xrickastley.sevenelements.mixin.client.StagedVertexBufferAccessor;
import io.github.xrickastley.sevenelements.util.Functions;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public abstract class SevenElementsRenderer<S> {
	private static final List<ByteBufferBuilder> ALLOCATORS = new ArrayList<>();
	private static final List<StagedVertexBuffer> STAGED_VERTEX_BUFFERS = new ArrayList<>();
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
	private final Map<RenderPipeline, Map<Integer, StagedVertexBuffer>> pipelineAllocators = new HashMap<>();
	private final List<RenderStage<S>> renderStages = new ArrayList<>();
	private final List<RenderDraw> renderDraws = new ArrayList<>();
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

	protected void registerRenderStage(RenderPipeline pipeline, int bindingIndex, int bufferSize, RenderStageFunction<S> renderFunction) {
		this.renderStages.add(
			new RenderStage<S>(this, pipeline, bindingIndex, bufferSize, renderFunction)
		);
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

	@SuppressWarnings("deprecation")
	protected final void render(LevelRenderContext context) {
		if (this.hasFlag(SevenElementsRenderer.LEGACY_TRANSFORMS)) {
			final PoseStack matrices = context.poseStack();
			final Camera camera = context.gameRenderer().mainCamera();

			matrices.pushPose();
			matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot()));
			matrices.mulPose(Axis.YP.rotationDegrees(camera.yRot() + 180.0F));
		}

		this.beforeRender(context);

		this.renderStages(context);
		this.states.forEach(state -> this.render(context, state));

		if (this.hasFlag(SevenElementsRenderer.CLEAR_RENDER_STATES))
			this.states.clear();

		if (this.hasFlag(SevenElementsRenderer.LEGACY_TRANSFORMS))
			context.poseStack().popPose();

		this.afterRender(context);
	}

	/**
	 * Gets a {@code VertexConsumer} for the provided {@code RenderPipeline}.
	 *
	 * <p>If there is none, this method automatically creates a {@code StagedVertexBuffer} instance
	 * for the returned {@code VertexConsumer}, which is automatically closed when the game is
	 * closed.
	 *
	 * <p>Do note that each instance of the {@code VertexConsumer} is backed by the same
	 * {@code StagedVertexBuffer} instance, which may cause rendering issues when two default buffer
	 * builder instances are written to.
	 *
	 * <p>If you need a {@code VertexConsumer} with a <i>unique</i> {@code StagedVertexBuffer} instance
	 * for the same {@code RenderPipeline}, use the second overload and define a new vertex binding
	 * inside the {@code RenderPipeline}.
	 *
	 * @param pipeline The {@code RenderPipeline} to get a {@code VertexConsumer} for.
	 * @param bufferSize The size of the {@code StagedVertexBuffer}, if none exists yet.
	 * @return A {@code VertexConsumer} for the provided {@code RenderPipeline}.
	 */
	@Deprecated
	protected final VertexConsumer getVertexBuilder(RenderPipeline pipeline, int bufferSize) {
		return this.getVertexBuilder(pipeline, 0, bufferSize);
	}

	/**
	 * Gets a {@code VertexConsumer} with the provided {@code bindingIndex} for the provided
	 * {@code RenderPipeline}.
	 *
	 * <p>A {@code bindingIndex} corresponds to a single, unique {@code StagedVertexBuffer}
	 * instance for the corresponding vertex binding in the defined {@code RenderPipeline}.
	 *
	 * <p>If there is none, this method automatically creates a {@code StagedVertexBuffer} instance
	 * for the returned {@code VertexConsumer}, which is automatically closed when the game is
	 * closed.
	 *
	 * <p>This method will also <b>create</b> a {@code StagedVertexBuffer.Draw} instance via
	 * {@link StagedVertexBuffer#appendDraw} and get a single {@code VertexConsumer} from it.
	 *
	 * @param pipeline The {@code RenderPipeline} to get a {@code VertexConsumer} for.
	 * @param bindingIndex A unique integer id to refer to a vertex binding in the defined {@code RenderPipeline} instance.
	 * @param bufferSize The size of the {@code StagedVertexBuffer}, if none exists yet.
	 * @return A {@code VertexConsumer} using the provided {@code bindingIndex} for the provided {@code RenderPipeline}.
	 */
	@Deprecated
	protected final VertexConsumer getVertexBuilder(RenderPipeline pipeline, int bindingIndex, int bufferSize) {
		return this.getStagedVertexBuffer(pipeline, bindingIndex, bufferSize)
			.getVertexBuilder(this.appendDraw(pipeline, bindingIndex, bufferSize));
	}

	/**
	 * Gets a {@code VertexConsumer} for the provided {@code StagedVertexBuffer}.
	 *
	 * <p>This method will also <b>create</b> a {@code StagedVertexBuffer.Draw} instance via
	 * {@link StagedVertexBuffer#appendDraw} and get a single {@code VertexConsumer} from it.
	 *
	 * <p>Additionally, the provided {@code StagedVertexBuffer} instance must be "registered" to
	 * this renderer. Instances attached to the {@code RenderFunction} are typically registered.
	 *
	 * @param stagedBuffer The staged vertex buffer to get a vertex consumer from.
	 * @return A vertex consumer instance from the staged buffer.
	 */
	protected final VertexConsumer getVertexBuilder(StagedVertexBuffer stagedBuffer) {
		final RenderStage<S> stage = this.renderStages.stream().filter(s -> s.stagedBuffer == stagedBuffer).findFirst().orElse(null);

		if (stage == null)
			throw new IllegalArgumentException("The provided stagedBuffer is not part of a render stage!");

		return stagedBuffer.getVertexBuilder(this.appendDraw(stage.pipeline, stage.bindingIndex, stage.bufferSize));
	}

	/**
	 * Gets a {@code VertexConsumer} for the provided {@code StagedVertexBuffer} using the provided
	 * {@code RenderType}.
	 *
	 * <p>This method will also <b>create</b> a {@code StagedVertexBuffer.Draw} instance via
	 * {@link StagedVertexBuffer#appendDraw} and get a single {@code VertexConsumer} from it when
	 * possible.
	 *
	 * <p>Additionally, the provided {@code StagedVertexBuffer} instance must be "registered" to
	 * this renderer. Instances attached to the {@code RenderFunction} are typically registered.
	 *
	 * @param stagedBuffer The staged vertex buffer to get a vertex consumer from.
	 * @param renderType The render type to use in creating the vertex consumer.
	 * @return A vertex consumer instance from the staged buffer using the provided render type.
	 */
	protected final VertexConsumer getVertexBuilder(StagedVertexBuffer stagedBuffer, RenderType renderType) {
		return this.getVertexBuilder(stagedBuffer, renderType, false);
	}

	/**
	 * Gets a {@code VertexConsumer} for the provided {@code StagedVertexBuffer} using the provided
	 * {@code RenderType}.
	 *
	 * <p>This method will also <b>create</b> a {@code StagedVertexBuffer.Draw} instance via
	 * {@link StagedVertexBuffer#appendDraw} and get a single {@code VertexConsumer} from it when
	 * possible.
	 *
	 * <p>Additionally, the provided {@code StagedVertexBuffer} instance must be "registered" to
	 * this renderer. Instances attached to the {@code RenderFunction} are typically registered.
	 *
	 * @param stagedBuffer The staged vertex buffer to get a vertex consumer from.
	 * @param renderType The render type to use in creating the vertex consumer.
	 * @param force Whether to force a new vertex consumer to be created.
	 * @return A vertex consumer instance from the staged buffer using the provided render type.
	 */
	protected final VertexConsumer getVertexBuilder(StagedVertexBuffer stagedBuffer, RenderType renderType, boolean force) {
		final RenderStage<S> stage = this.renderStages.stream().filter(s -> s.stagedBuffer == stagedBuffer).findFirst().orElse(null);

		if (stage == null)
			throw new IllegalArgumentException("The provided stagedBuffer is not part of a render stage!");

		StagedVertexBuffer.Draw draw;
		if (!this.renderDraws.isEmpty() && this.renderDraws.getLast().type.equals(renderType) && !force) {
			draw = this.renderDraws.getLast().draw();
		} else {
			final VertexSorting quadSorting = renderType.sortOnUpload()
				? RenderSystem.getProjectionType().vertexSorting()
				: null;

			draw = stagedBuffer.appendDraw(renderType.format(), renderType.primitiveTopology(), quadSorting);

			this.renderDraws.add(new RenderDraw(draw, renderType));
		}

		return stagedBuffer.getVertexBuilder(draw);
	}

	/**
	 * Gets a {@code VertexConsumer} with the provided {@code id} for the provided
	 * {@code RenderPipeline}.
	 *
	 * <p>A {@code bindingIndex} corresponds to a single, unique {@code StagedVertexBuffer}
	 * instance for the corresponding vertex binding in the defined {@code RenderPipeline}.
	 *
	 * <p>If there is none, this method automatically creates a {@code StagedVertexBuffer} instance
	 * for the returned {@code VertexConsumer}, which is automatically closed when the game is
	 * closed.
	 *
	 * <p>This method will also <b>create</b> a {@code StagedVertexBuffer.Draw} instance via
	 * {@link StagedVertexBuffer#appendDraw}.
	 *
	 * @param pipeline The {@code RenderPipeline} to get a {@code VertexConsumer} for.
	 * @param bindingIndex A unique integer id to refer to a vertex binding in the defined {@code RenderPipeline} instance.
	 * @param bufferSize The size of the {@code StagedVertexBuffer}, if none exists yet.
	 * @return A {@code VertexConsumer} using the provided {@code bindingIndex} for the provided {@code RenderPipeline}.
	 */
	protected final StagedVertexBuffer.Draw appendDraw(RenderPipeline pipeline, int bindingIndex, int bufferSize) {
		final PrimitiveTopology primitiveTopology = pipeline.getPrimitiveTopology();
		final VertexFormat vertexFormat = Objects.requireNonNull(pipeline.getVertexFormatBinding(bindingIndex), "No vertex format binding with id: " + bindingIndex + " exists in the RenderPipeline: " + pipeline.toString() + "!");

		return this
			.getStagedVertexBuffer(pipeline, bindingIndex, bufferSize)
			.appendDraw(vertexFormat, primitiveTopology, primitiveTopology == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null);
	}

	protected final StagedVertexBuffer getStagedVertexBuffer(RenderPipeline pipeline, int bindingIndex, int bufferSize) {
		return this.pipelineAllocators
			.computeIfAbsent(pipeline, p-> new HashMap<>())
			.computeIfAbsent(bindingIndex, i -> new StagedVertexBuffer(Functions.supplier(this.getClass().getSimpleName() + " Buffer / " + pipeline.toString()), bufferSize));
	}

	private void renderStages(LevelRenderContext context) {
		for (final RenderStage<S> stage : this.renderStages) {
			this.states.forEach(state -> stage.renderFunction.render(context, stage.stagedBuffer, state));

			this.draw(stage.stagedBuffer, stage.pipeline);
		}
	}

	/**
	 * This method is called per render state to be rendered individually.
	 *
	 * @param context The current world rendering context.
	 * @param state The render state to be rendered.
	 * @deprecated Register a render stage via {@link SevenElementsRenderer#registerRenderStage()} instead!
	 */
	@Deprecated
	protected void render(LevelRenderContext context, S state) {};

	/**
	 * Build and draws the provided {@code stagedBuffer} in the world using the provided
	 * {@code RenderPipeline}.
	 *
	 * @param stagedBuffer The {@code StagedVertexBuffer} to build and draw.
	 * @param pipeline The {@code RenderPipeline} to use.
	 */
	protected final void draw(StagedVertexBuffer stagedBuffer, RenderPipeline pipeline) {
		this.draw(stagedBuffer, pipeline, Optional.empty());
	}

	/**
	 * Build and draws the provided {@code stagedBuffer} in the world using the provided
	 * {@code RenderPipeline}.
	 *
	 * <p>This also binds the provided {@code texture}, if existent, to {@code Sampler0}.
	 *
	 * @param buffer The {@code StagedVertexBuffer} to build and draw.
	 * @param pipeline The {@code RenderPipeline} to use.
	 * @param texture The texture to bind to {@code Sampler0}, if existent.
	 */
	protected final void draw(StagedVertexBuffer stagedBuffer, RenderPipeline pipeline, Optional<Identifier> texture) {
		if (!this.pipelineAllocators.get(pipeline).containsValue(stagedBuffer))
			throw new IllegalArgumentException("The provided stagedBuffer does not exist in the provided RenderPipeline!");

		final Map<StagedVertexBuffer.Draw, RenderType> drawTypeMap = this.renderDraws
			.stream()
			.collect(Collectors.toMap(RenderDraw::draw, RenderDraw::type));

		stagedBuffer.upload();

		((StagedVertexBufferAccessor) stagedBuffer)
			.getDraws()
			.forEach(draw -> {
				final StagedVertexBuffer.ExecuteInfo info = stagedBuffer.getExecuteInfo(draw);

				if (drawTypeMap.containsKey(draw))
					drawTypeMap.get(draw).prepare().drawFromBuffer(info);
				else
					this.drawByPipeline(info, pipeline, texture);
			});

		this.renderDraws.clear();

		stagedBuffer.endFrame();
	}

	private final void drawByPipeline(StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline, Optional<Identifier> texture) {
		final Minecraft client = Minecraft.getInstance();

		final RenderTarget framebuffer = client.gameRenderer.mainRenderTarget();
		final GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
			.writeTransform(
				RenderSystem.getModelViewMatrixCopy(),
				new Vector4f(1f, 1f, 1f, 1f),
				new Vector3f(),
				new Matrix4f()
			);

		try (
			RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(
					Functions.supplier("Draw for SevenElementsRenderer instance: " + this.getClass().getSimpleName() + " with RenderPipeline: " + pipeline.getLocation()), framebuffer.getColorTextureView(), Optional.empty(), framebuffer.getDepthTextureView(), OptionalDouble.empty()
				)
		) {
			renderPass.setPipeline(pipeline);

			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);

			if (texture.isPresent())
				renderPass.bindTexture("Sampler0", client.getTextureManager().getTexture(texture.get()).getTextureView(), null);

			renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
			renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());

			renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
		}
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
		return buffer == null || !((BufferBuilderAccessor) buffer).sevenelements$isBuilding()
			? new BufferBuilder(allocator, pipeline.getPrimitiveTopology(), pipeline.getVertexFormatBinding(0))
			: buffer;
	}

	public static ByteBufferBuilder createAllocator(final int size) {
		final ByteBufferBuilder allocator = new ByteBufferBuilder(size);

		SevenElementsRenderer.ALLOCATORS.add(allocator);

		return allocator;
	}

	public static StagedVertexBuffer createStagedBuffer(final String label, final int size) {
		final StagedVertexBuffer stagedBuffer = new StagedVertexBuffer(Functions.supplier(label), size);

		SevenElementsRenderer.STAGED_VERTEX_BUFFERS.add(stagedBuffer);

		return stagedBuffer;
	}

	public static void close() {
		SevenElementsRenderer.ALLOCATORS.forEach(ByteBufferBuilder::close);
		SevenElementsRenderer.STAGED_VERTEX_BUFFERS.forEach(StagedVertexBuffer::close);
		SevenElementsRenderer.SINGLETON_MAP
			.values()
			.forEach(renderer ->
				renderer.pipelineAllocators
					.values()
					.forEach(allocators ->
						allocators
							.values()
							.forEach(StagedVertexBuffer::close)
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

	@FunctionalInterface
	protected static interface RenderStageFunction<S> {
		void render(LevelRenderContext context, StagedVertexBuffer stagedBuffer, S state);
	}

	private static record RenderStage<S>(StagedVertexBuffer stagedBuffer, RenderPipeline pipeline, int bindingIndex, int bufferSize, RenderStageFunction<S> renderFunction) {
		RenderStage(SevenElementsRenderer<S> renderer, RenderPipeline pipeline, int bindingIndex, int bufferSize, RenderStageFunction<S> renderFunction) {
			this(renderer.getStagedVertexBuffer(pipeline, bindingIndex, bufferSize), pipeline, bindingIndex, bufferSize, renderFunction);
		}
	}

	private static record RenderDraw(StagedVertexBuffer.Draw draw, RenderType type) {}
}
