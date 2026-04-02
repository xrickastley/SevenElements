package io.github.xrickastley.sevenelements.renderer.genshin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.networking.PayloadHandler;
import io.github.xrickastley.sevenelements.networking.ShowElectroChargeS2CPayload;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderPipelines;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderer;
import io.github.xrickastley.sevenelements.util.BoxUtil;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.Color;
import io.github.xrickastley.sevenelements.util.Colors;
import io.github.xrickastley.sevenelements.util.Ease;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;
import io.github.xrickastley.sevenelements.util.polyfill.rendering.WorldRenderContext;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SpecialEffectsRenderer implements PayloadHandler<ShowElectroChargeS2CPayload> {
	private static final int MAX_TICKS = 10;
	private static final double POISSON_DENSITY = 1.5;
	private static final RandomSource RANDOM = RandomSource.create();
	private static final int CHARGE_ITERATIONS = 4;
	private static final ByteBufferBuilder allocator = SevenElementsRenderer.createAllocator(RenderType.BIG_BUFFER_SIZE);
	private final List<Entry> entries = new ArrayList<>();
	private final Multimap<LivingEntity, ChargeLinePositions> chargePositions = HashMultimap.create();

	/**
	 * Returns whether effects should be rendered for the provided entity.
	 * @param entity The entity planned to render effects for.
	 */
	public static boolean shouldRender(Entity entity) {
		final Minecraft client = Minecraft.getInstance();

		return entity.isAlive()
			&& (entity != client.player || client.gameRenderer.getMainCamera().isDetached());
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

		entries.add(
			new ElectroChargedEffect(
				mainEntity,
				payload
					.otherEntities()
					.stream()
					.map(world::getEntity)
					.filter(e -> e != null)
					.toList()
			)
		);
	}

	public void render(WorldRenderContext context) {
		entries.forEach(Functions.withArgument(Entry::render, context, this));

		this.renderEffects(context);
	}

	public void tick(ClientLevel world) {
		this.entries.removeIf(Entry::shouldRemove);
		this.entries.forEach(Entry::tick);

		if (world.getGameTime() % 10 == 0) this.chargePositions.clear();

		this.chargePositions
			.values()
			.forEach(ChargeLinePositions::clearPositions);
	}

	private Collection<ChargeLinePositions> getChargePositions(LivingEntity entity) {
		final Collection<ChargeLinePositions> mapValue = this.chargePositions.get(entity);

		if (!mapValue.isEmpty()) return mapValue;

		final List<ChargeLinePositions> computedValue = new ArrayList<>();
		final AABB box = BoxUtil.multiplyBox(entity.getBoundingBox(), 0.75);

		for (int i = 0; i < SpecialEffectsRenderer.CHARGE_ITERATIONS + 2; i++) {
			final Vec3 initialPos = BoxUtil.randomPos(box);
			final Vec3 finalPos = BoxUtil.randomPos(box);

			computedValue.add(new ChargeLinePositions(initialPos, finalPos, entity));
		}

		this.chargePositions.putAll(entity, computedValue);

		return computedValue;
	}

	private void renderQuickenAura(WorldRenderContext context, LivingEntity entity) {
		if (!ClientConfig.getEffectRenderType().allowsSpecialEffects()) return;

		this.getChargePositions(entity)
			.forEach(clp -> {
				final Color color = clp.computeColorIfAbsent(() -> Math.random() < 0.5 ? Colors.ELECTRO : Colors.DENDRO);

				this.renderChargeLine(context, clp, entity, color, Colors.PHYSICAL);
			});
	}

	private void renderElectroAura(WorldRenderContext context, LivingEntity entity) {
		if (!ClientConfig.getEffectRenderType().allowsNormalEffects()) return;

		this.getChargePositions(entity)
			.forEach(clp -> this.renderChargeLine(context, clp, entity, Colors.ELECTRO, Colors.PHYSICAL));
	}

	private void renderEffects(WorldRenderContext context) {
		for (final Entity entity : context.world().entitiesForRendering()) {
			if (!(entity instanceof final LivingEntity livingEntity) || !shouldRender(livingEntity)) continue;

			final ElementComponent component = ElementComponent.KEY.get(livingEntity);

			if (component.hasElementalApplication(Element.QUICKEN)) this.renderQuickenAura(context, livingEntity);
			else if (component.hasElementalApplication(Element.ELECTRO)) this.renderElectroAura(context, livingEntity);
		}
	}

	private void renderChargeLine(WorldRenderContext context, ChargeLinePositions clp, Entity entity, Color outerColor, Color innerColor) {
		final Vec3 initialPos = clp.getInitialPos(entity);

		this.renderChargeLine(context, initialPos, clp.generatePositions(this, entity), outerColor, innerColor);
	}

	@SuppressWarnings("unused")
	private void renderChargeLine(WorldRenderContext context, Vec3 initialPos, Vec3 finalPos, Color outerColor, Color innerColor) {
		final List<Vec3> positions = this.generatePositions(Vec3.ZERO, initialPos.subtract(finalPos));

		Vec3 randomVec = Vec3.ZERO;

		for (int i = 0; i < positions.size(); i++) {
			randomVec = new Vec3(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5);

			positions.set(i, positions.get(i).add(randomVec));
		}

		positions.add(0, Vec3.ZERO);
		positions.add(finalPos.subtract(initialPos));

		this.renderChargeLine(context, initialPos, positions, outerColor, innerColor);
	}

	private void renderChargeLine(WorldRenderContext context, Vec3 origin, List<Vec3> positions, Color outerColor, Color innerColor) {
		final Camera camera = context.camera();
		final Vec3 camPos = camera.position();

		final PoseStack matrices = new PoseStack();
		matrices.pushPose();
		matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot()));
		matrices.mulPose(Axis.YP.rotationDegrees(camera.yRot() + 180.0F));
		matrices.translate(origin.x - camPos.x, origin.y - camPos.y, origin.z - camPos.z);

		final Matrix4f posMat = matrices.last().pose();
		final PoseStack.Pose entry = matrices.last();

		BufferBuilder buffer = SevenElementsRenderer.createBuffer(allocator, SevenElementsRenderPipelines.CHARGE_LINE);

		for (int i = 1; i < positions.size(); i++) {
			final Vec3 start = positions.get(i - 1);
			final Vec3 end = positions.get(i);
			Vec3 normal = end.normalize();

			buffer
				.addVertex(posMat, (float) start.x, (float) start.y, (float) start.z)
				.setColor(outerColor.asARGB())
				.setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
				.setLineWidth(6.0f);

			buffer
				.addVertex(posMat, (float) end.x, (float) end.y, (float) end.z)
				.setColor(outerColor.asARGB())
				.setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
				.setLineWidth(6.0f);
		}

		SevenElementsRenderLayer.getChargeLine().draw(buffer.buildOrThrow());

		buffer = SevenElementsRenderer.createBuffer(allocator, SevenElementsRenderPipelines.CHARGE_LINE);

		for (int i = 1; i < positions.size(); i++) {
			final Vec3 start = positions.get(i - 1);
			final Vec3 end = positions.get(i);
			Vec3 normal = end.normalize();

			buffer
				.addVertex(posMat, (float) start.x, (float) start.y, (float) start.z)
				.setColor(innerColor.asARGB())
				.setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
				.setLineWidth(2.0f);

			buffer
				.addVertex(posMat, (float) end.x, (float) end.y, (float) end.z)
				.setColor(innerColor.asARGB())
				.setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
				.setLineWidth(2.0f);
		}

		SevenElementsRenderLayer.getChargeLine().draw(buffer.buildOrThrow());

		matrices.popPose();
	}

	private List<Vec3> generatePositions(final Vec3 initialPos, final Vec3 finalPos) {
		final Vec3 norm = initialPos.subtract(finalPos);
		final double length = norm.length();

		final int n = Math.max(1, this.poisson(SpecialEffectsRenderer.POISSON_DENSITY * length));
		final List<Double> doubles = new ArrayList<>();

		for (int i = 0; i < n; i++) doubles.add(RANDOM.nextDouble());

		return doubles
			.stream()
			.sorted()
			.map(t -> initialPos.add(norm.scale(t)).add(new Vec3(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5)))
			.collect(Collectors.toList());
	}

	private int poisson(double lambda) {
		final double L = Math.exp(-lambda);

		int k = 0;
		double p = 1.0;
		do {
			k++;
			p *= SpecialEffectsRenderer.RANDOM.nextDouble();
		} while (p > L);

		return k - 1;
	}

	private static abstract class Entry {
		abstract boolean shouldRemove();
		abstract void render(final WorldRenderContext context, final SpecialEffectsRenderer renderer);
		void tick() {};
	}

	private static class ElectroChargedEffect extends Entry {
		private final long time;
		private final Entity mainEntity;
		private final List<Entity> otherEntities;
		private final Map<Entity, StoredElectroChargedPositions> positionMap = new HashMap<>();

		private ElectroChargedEffect(Entity mainEntity, List<Entity> otherEntities) {
			this.time = Minecraft.getInstance().level.getGameTime();
			this.mainEntity = mainEntity;
			this.otherEntities = otherEntities;
		}

		boolean shouldRemove() {
			return !mainEntity.isAlive() || otherEntities.isEmpty() || Minecraft.getInstance().level.getGameTime() > this.time + MAX_TICKS;
		}

		void render(final WorldRenderContext context, final SpecialEffectsRenderer renderer) {
			final double gradientStep = Mth.clamp(Mth.inverseLerp(Minecraft.getInstance().level.getGameTime() - this.time + context.tickCounter().getGameTimeDeltaPartialTick(false), 0, 10), 0, 1);
			final Color outerColor = Color.gradientStep(Colors.ELECTRO, Colors.HYDRO, gradientStep, Ease.IN_QUART);
			final Color innerColor = Colors.PHYSICAL;

			for (final Entity other : this.otherEntities) {
				if (other == this.mainEntity) continue;

				final StoredElectroChargedPositions entry = positionMap.computeIfAbsent(other, o -> new StoredElectroChargedPositions(this.mainEntity, other));

				renderer.renderChargeLine(context, entityPos(this.mainEntity), entry.generatePositions(renderer), outerColor, innerColor);
			}
		}

		@Override
		void tick() {
			super.tick();

			this.positionMap.values().forEach(StoredElectroChargedPositions::tick);
		}

		private Vec3 entityPos(Entity entity) {
			return entity.position().add(0, entity.getBbHeight() * 0.5, 0);
		}
	}

	private static class StoredElectroChargedPositions {
		private final Entity mainEntity;
		private final Entity targetEntity;
		private Vec3 prevMainEntityPos;
		private Vec3 prevTargetEntityPos;
		private @Nullable List<Vec3> positions = null;

		private StoredElectroChargedPositions(Entity mainEntity, Entity targetEntity) {
			this.mainEntity = mainEntity;
			this.targetEntity = targetEntity;
			this.prevMainEntityPos = this.entityPos(mainEntity);
			this.prevTargetEntityPos = this.entityPos(targetEntity);
		}

		private void tick() {
			this.positions = null;
		}

		private boolean shouldPositionsPersist() {
			return this.entityPos(mainEntity).equals(prevMainEntityPos)
				&& this.entityPos(targetEntity).equals(prevTargetEntityPos);
		}

		private List<Vec3> generatePositions(SpecialEffectsRenderer renderer) {
			if (this.positions != null && shouldPositionsPersist()) return this.positions;

			// Required unequal due to shouldPositionsPersist(), refresh
			final Vec3 initialPos = this.prevMainEntityPos = this.entityPos(this.mainEntity);
			final Vec3 finalPos = this.prevTargetEntityPos = this.entityPos(this.targetEntity);

			final List<Vec3> positions = renderer.generatePositions(Vec3.ZERO, initialPos.subtract(finalPos));

			Vec3 randomVec = Vec3.ZERO;

			for (int i = 0; i < positions.size(); i++) {
				randomVec = new Vec3(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5);

				positions.set(i, positions.get(i).add(randomVec));
			}

			positions.add(0, Vec3.ZERO);
			positions.add(finalPos.subtract(initialPos));

			this.positions = positions;

			return positions;
		}

		private Vec3 entityPos(Entity entity) {
			return entity.position().add(0, entity.getBbHeight() * 0.5, 0);
		}
	}

	private static class ChargeLinePositions {
		private final Vec3 initialPos;
		private final Vec3 finalPos;
		private @Nullable List<Vec3> positions = null;
		private @Nullable Color color = null;

		private ChargeLinePositions(Vec3 initialPos, Vec3 finalPos, Entity relativeTo) {
			final Vec3 entityPos = relativeTo.position();

			this.initialPos = initialPos.subtract(entityPos);
			this.finalPos = finalPos.subtract(entityPos);
		}

		private Vec3 getInitialPos(Entity relativeTo) {
			return this.initialPos.add(relativeTo.position());
		}

		private List<Vec3> generatePositions(SpecialEffectsRenderer renderer, Entity relativeTo) {
			if (this.positions != null) return this.positions;

			final Vec3 entityPos = relativeTo.position();
			final List<Vec3> positions = renderer.generatePositions(Vec3.ZERO, initialPos.subtract(finalPos));

			Vec3 randomVec = Vec3.ZERO;

			for (int i = 0; i < positions.size(); i++) {
				randomVec = new Vec3(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5);

				positions.set(i, positions.get(i).add(randomVec));
			}

			positions.add(0, Vec3.ZERO);
			positions.add(finalPos.subtract(initialPos));

			this.positions = positions;

			return positions
				.stream()
				.map(Functions.<Vec3, Vec3, Vec3>withArgument(Vec3::add, entityPos))
				.toList();
		}

		private void clearPositions() {
			this.positions = null;
		}

		private @Nullable Color getColor() {
			return this.color;
		}

		private Color computeColorIfAbsent(Supplier<Color> ifAbsent) {
			return this.color = JavaScriptUtil.nullishCoalesing(this.color, ifAbsent.get());
		}
	}

	static {

	}
}
