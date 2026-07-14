package io.github.xrickastley.sevenelements.renderer.genshin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.networking.PayloadHandler;
import io.github.xrickastley.sevenelements.networking.ShowElectroChargeS2CPayload;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderPipelines;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderer;
import io.github.xrickastley.sevenelements.renderer.state.ChargeEffectState;
import io.github.xrickastley.sevenelements.util.BoxUtil;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Color;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.Ease;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;
import io.github.xrickastley.sevenelements.util.MathHelper2;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ChargeEffectRenderer
	extends SevenElementsRenderer<ChargeEffectState>
	implements PayloadHandler<ShowElectroChargeS2CPayload>
{
	private static final double POISSON_DENSITY = 1.5;
	private static final double BOUNDING_BOX_SCALE = 0.75;
	private static final int CHARGE_LINE_ITERATIONS = 6;

	private static final int MAX_ELECTRO_CHARGE_TICKS = 10;

	private final Multimap<Entity, EffectEntry> effectEntryCache = HashMultimap.create();
	private final Multimap<LivingEntity, ChargeLinePositions> auraCache = HashMultimap.create();

	public ChargeEffectRenderer() {
		super(SevenElementsRenderer.CLEAR_RENDER_STATES);

		this.registerRenderStage(SevenElementsRenderPipelines.CHARGE_LINE, 0, RenderType.SMALL_BUFFER_SIZE, this::render);
	}

	@Override
	public CustomPacketPayload.Type<ShowElectroChargeS2CPayload> getPayloadId() {
		return ShowElectroChargeS2CPayload.ID;
	}

	@Override
	public void receive(ShowElectroChargeS2CPayload payload, Context context) {
		final LocalPlayer player = context.player();
		final Level world = player.level();
		final Entity mainEntity = world.getEntity(payload.mainEntity());

		if (mainEntity == null) {
			SevenElements.sublogger().warn("Received packet for unknown main Electro-Charged entity, ignoring!");

			return;
		}

		this.effectEntryCache.put(
			mainEntity,
			new ElectroChargedEffect(
				mainEntity,
				payload
					.otherEntities()
					.stream()
					.map(world::getEntity)
					.filter(Objects::nonNull)
					.toList()
			)
		);
	}



	@Override
	protected void extract(LevelExtractionContext context) {
		super.extract(context);

		StreamSupport.stream(context.level().entitiesForRendering().spliterator(), false)
			.map(entity -> ClassInstanceUtil.castOrNull(entity, LivingEntity.class))
			.filter(Objects::nonNull)
			.forEachOrdered(Functions.withArgument(this::extractEntity, context.deltaTracker().getGameTimeDeltaPartialTick(false)));
	}

	@Override
	protected void beforeRender(LevelRenderContext context) {
		super.beforeRender(context);

		final PoseStack matrices = context.poseStack();
		final Camera camera = context.gameRenderer().mainCamera();
		final Vec3 camPos = camera.position();

		matrices.pushPose();
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);
	}

	private void render(LevelRenderContext context, StagedVertexBuffer buffer, ChargeEffectState state) {
		final PoseStack matrices = context.poseStack();
		final Matrix4f posMat = matrices.last().pose();
		final PoseStack.Pose entry = matrices.last();

		final VertexConsumer lineBuffer = this.getVertexBuilder(buffer);

		for (int i = 1; i < state.positions.size(); i++) {
			final Vec3 start = state.positions.get(i - 1);
			final Vec3 end = state.positions.get(i);
			final Vec3 normal = end.normalize();

			lineBuffer
				.addVertex(posMat, (float) start.x, (float) start.y, (float) start.z)
				.setColor(state.outerColor.asARGB())
				.setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
				.setLineWidth(6.0f)
				.addVertex(posMat, (float) end.x, (float) end.y, (float) end.z)
				.setColor(state.outerColor.asARGB())
				.setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
				.setLineWidth(6.0f);

			lineBuffer
				.addVertex(posMat, (float) start.x, (float) start.y, (float) start.z)
				.setColor(state.innerColor.asARGB())
				.setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
				.setLineWidth(2.0f)
				.addVertex(posMat, (float) end.x, (float) end.y, (float) end.z)
				.setColor(state.innerColor.asARGB())
				.setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
				.setLineWidth(2.0f);
		}
	}

	@Override
	protected void afterRender(LevelRenderContext context) {
		super.afterRender(context);

		context.poseStack().popPose();
	}

	@Override
	protected void tick(ClientLevel world) {
		super.tick(world);

		if (world.getGameTime() % 10 == 0)
			this.auraCache.clear();

		this.auraCache
			.values()
			.forEach(ChargeLinePositions::clearPositions);

		final Iterator<EffectEntry> entryIterator = this.effectEntryCache.values().iterator();

		while (entryIterator.hasNext()) {
			final EffectEntry entry = entryIterator.next();

			entry.tick();

			if (entry.shouldRemove())
				entryIterator.remove();
		}
	}

	private void extractEntity(final Entity entity, final float tickDelta) {
		this.extractEffectsFor(entity, tickDelta);
		this.extractElementalAura(entity);
	}

	private void extractEffectsFor(final Entity entity, final float tickDelta) {
		this.effectEntryCache.get(entity)
			.stream()
			.filter(Functions.withArgument(EffectEntry::shouldExtractFor, entity))
			.forEach(Functions.withArgument(EffectEntry::extract, entity, tickDelta));
	}

	private void extractElementalAura(final Entity entity) {
		if (!(entity instanceof final LivingEntity livingEntity) || !SpecialEffectsRenderer.shouldRender(entity)) return;

		final ElementComponent component = ElementComponent.KEY.get(livingEntity);

		if (!component.hasElementalApplication(Element.QUICKEN) && !component.hasElementalApplication(Element.ELECTRO))
			return;

		final Collection<ChargeLinePositions> chargePositions = this.getOrCreatePositions(livingEntity);
		final Supplier<Color> colorSupplier = component.hasElementalApplication(Element.QUICKEN)
			? () -> Math.random() < 0.5 ? Colors.ELECTRO : Colors.DENDRO
			: Functions.supplier(Colors.ELECTRO);

		chargePositions
			.stream()
			.forEach(positions -> {
				final ChargeEffectState state = new ChargeEffectState();

				state.positions = positions.getPositions()
					.stream()
					.map(pos -> pos.add(positions.initialPos).add(livingEntity.position()))
					.toList();

				state.outerColor = positions.computeColorIfAbsent(colorSupplier);
				state.innerColor = positions.computeColorIfAbsent(Functions.supplier(Colors.PHYSICAL));

				this.addRenderState(state);
			});
	}

	private Collection<ChargeLinePositions> getOrCreatePositions(LivingEntity entity) {
		if (!this.auraCache.containsKey(entity)) {
			final AABB box = BoxUtil.multiplyBox(entity.getBoundingBox(), ChargeEffectRenderer.BOUNDING_BOX_SCALE);

			Stream
				.generate(() -> {
					final Vec3 initialPos = BoxUtil.randomPos(box);
					final Vec3 finalPos = BoxUtil.randomPos(box);
					return new ChargeLinePositions(initialPos, finalPos, entity);
				})
				.limit(ChargeEffectRenderer.CHARGE_LINE_ITERATIONS)
				.forEach(position -> this.auraCache.put(entity, position));
		}

		return this.auraCache.get(entity);
	}

	private static List<Vec3> generateRandomPositions(Vec3 initialPos, Vec3 finalPos, double density) {
		final Vec3 norm = finalPos.subtract(initialPos);

		return Stream.generate(Math::random)
			.limit(Math.max(1, MathHelper2.randomPoissonInt(density * norm.length())))
			.sorted()
			.map(m ->
				initialPos
					.add(norm.scale(m))
					.add(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5)
			)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	private static List<Vec3> generatePositionsTo(Vec3 initialPos, Vec3 finalPos, double density) {
		final List<Vec3> positions = generateRandomPositions(initialPos, finalPos, ChargeEffectRenderer.POISSON_DENSITY);

		positions.add(0, initialPos);
		positions.add(finalPos);

		return positions;
	}



	private class ChargeLinePositions {
		private final Vec3 initialPos;
		private final Vec3 finalPos;
		private @Nullable List<Vec3> positions;
		private @Nullable Color color = null;

		private ChargeLinePositions(Vec3 initialPos, Vec3 finalPos, LivingEntity relativeTo) {
			final Vec3 entityPos = relativeTo.position();

			this.initialPos = initialPos.subtract(entityPos);
			this.finalPos = finalPos.subtract(entityPos);
			this.positions = this.generatePositions();
		}

		private List<Vec3> getPositions() {
			return this.generatePositions();
		}

		private List<Vec3> generatePositions() {
			if (this.positions != null)
				return this.positions;

			this.positions = generatePositionsTo(Vec3.ZERO, finalPos.subtract(initialPos), ChargeEffectRenderer.POISSON_DENSITY);

			return this.positions;
		}

		private void clearPositions() {
			this.positions = null;
		}

		private @Nullable Color getColor() {
			return this.color;
		}

		private Color computeColorIfAbsent(Supplier<Color> ifAbsent) {
			return this.color = JavaScriptUtil.nullishCoalesingFn(Functions.supplier(this.color), ifAbsent);
		}
	}



	private abstract class EffectEntry {
		abstract boolean shouldRemove();

		abstract boolean shouldExtractFor(Entity entity);

		abstract void extract(Entity entity, float tickDelta);

		void tick() {};
	}

	private class ElectroChargedEffect extends EffectEntry {
		private final Entity mainEntity;
		private final Map<Entity, ElectroChargedPositions> positions = new HashMap<>();
		private int age;

		private ElectroChargedEffect(Entity mainEntity, List<Entity> otherEntities) {
			this.mainEntity = mainEntity;

			otherEntities.forEach(entity -> this.positions.put(entity, null));
			this.positions.remove(mainEntity);
		}

		@Override
		boolean shouldRemove() {
			return !this.mainEntity.isAlive()
				|| this.positions.keySet().isEmpty()
				|| this.age > ChargeEffectRenderer.MAX_ELECTRO_CHARGE_TICKS;
		}

		@Override
		boolean shouldExtractFor(Entity entity) {
			return this.mainEntity == entity;
		}

		@Override
		void extract(Entity entity, float tickDelta) {
			final double gradientStep = Mth.clamp(Mth.inverseLerp(this.age + tickDelta, 0, MAX_ELECTRO_CHARGE_TICKS), 0, 1);
			final Color outerColor = Color.gradientStep(Colors.ELECTRO, Colors.HYDRO, gradientStep, Ease.IN_QUART);
			final Color innerColor = Colors.PHYSICAL;

			this.positions
				.entrySet()
				.forEach(entry -> {
					final ElectroChargedPositions positions = ClassInstanceUtil.computeIfAbsent(entry.getValue(), () -> new ElectroChargedPositions(entity, entry.getKey()));
					final ChargeEffectState state = new ChargeEffectState();

					state.positions = positions.getPositions()
						.stream()
						.map(pos -> pos.add(positions.prevMainEntityPos))
						.toList();

					state.outerColor = outerColor;
					state.innerColor = innerColor;

					entry.setValue(positions);
					ChargeEffectRenderer.this.addRenderState(state);
				});
		}

		@Override
		void tick() {
			super.tick();

			this.age++;
			this.positions.values().stream()
				.filter(Objects::nonNull)
				.forEach(ElectroChargedPositions::tick);
		}
	}

	private class ElectroChargedPositions {
		private final Entity mainEntity;
		private final Entity targetEntity;
		private Vec3 prevMainEntityPos;
		private Vec3 prevTargetEntityPos;
		private @Nullable List<Vec3> positions = null;

		private ElectroChargedPositions(Entity mainEntity, Entity targetEntity) {
			this.mainEntity = mainEntity;
			this.targetEntity = targetEntity;
			this.prevMainEntityPos = this.centerEntityPos(mainEntity);
			this.prevTargetEntityPos = this.centerEntityPos(targetEntity);
		}

		private void tick() {
			this.positions = null;
		}

		private boolean shouldPositionsPersist() {
			return this.centerEntityPos(mainEntity).equals(prevMainEntityPos)
				&& this.centerEntityPos(targetEntity).equals(prevTargetEntityPos);
		}

		private List<Vec3> getPositions() {
			return this.generatePositions();
		}

		private List<Vec3> generatePositions() {
			if (this.positions != null && this.shouldPositionsPersist())
				return this.positions;

			// Currently unequal due to shouldPositionsPersist() = false, refresh them
			final Vec3 initialPos = this.prevMainEntityPos = this.centerEntityPos(this.mainEntity);
			final Vec3 finalPos = this.prevTargetEntityPos = this.centerEntityPos(this.targetEntity);

			this.positions = generatePositionsTo(Vec3.ZERO, finalPos.subtract(initialPos), ChargeEffectRenderer.POISSON_DENSITY);

			return positions;
		}

		private Vec3 centerEntityPos(Entity entity) {
			return entity.position().add(0, entity.getBbHeight() * 0.5, 0);
		}
	}
}
