package io.github.xrickastley.sevenelements.renderer.genshin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class SpecialEffectsRenderer implements PayloadHandler<ShowElectroChargeS2CPayload> {
	private static final int MAX_TICKS = 10;
	private static final double POISSON_DENSITY = 1.5;
	private static final Random RANDOM = Random.create();
	private static final int CHARGE_ITERATIONS = 4;
	private static final BufferAllocator allocator = SevenElementsRenderer.createAllocator(RenderLayer.SOLID_BUFFER_SIZE);
	private final List<Entry> entries = new ArrayList<>();
	private final Multimap<LivingEntity, ChargeLinePositions> chargePositions = HashMultimap.create();

	/**
	 * Returns whether effects should be rendered for the provided entity.
	 * @param entity The entity planned to render effects for.
	 */
	public static boolean shouldRender(Entity entity) {
		final MinecraftClient client = MinecraftClient.getInstance();

		return entity.isAlive()
			&& (entity != client.player || client.gameRenderer.getCamera().isThirdPerson());
	}

	@Override
	public CustomPayload.Id<ShowElectroChargeS2CPayload> getPayloadId() {
		return ShowElectroChargeS2CPayload.ID;
	}

	@Override
	public void receive(ShowElectroChargeS2CPayload payload, Context context) {
		final ClientPlayerEntity player = context.player();
		final World world = player.getWorld();
		final Entity mainEntity = world.getEntityById(payload.mainEntity());

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
					.map(world::getEntityById)
					.filter(e -> e != null)
					.toList()
			)
		);
	}

	public void render(WorldRenderContext context) {
		entries.forEach(Functions.withArgument(Entry::render, context, this));

		this.renderEffects(context);
	}

	public void tick(ClientWorld world) {
		this.entries.removeIf(Entry::shouldRemove);
		this.entries.forEach(Entry::tick);

		if (world.getTime() % 10 == 0) this.chargePositions.clear();

		this.chargePositions
			.values()
			.forEach(ChargeLinePositions::clearPositions);
	}

	private Collection<ChargeLinePositions> getChargePositions(LivingEntity entity) {
		final Collection<ChargeLinePositions> mapValue = this.chargePositions.get(entity);

		if (!mapValue.isEmpty()) return mapValue;

		final List<ChargeLinePositions> computedValue = new ArrayList<>();
		final Box box = BoxUtil.multiplyBox(entity.getBoundingBox(), 0.75);

		for (int i = 0; i < SpecialEffectsRenderer.CHARGE_ITERATIONS + 2; i++) {
			final Vec3d initialPos = BoxUtil.randomPos(box);
			final Vec3d finalPos = BoxUtil.randomPos(box);

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
		for (final Entity entity : context.world().getEntities()) {
			if (!(entity instanceof final LivingEntity livingEntity) || !shouldRender(livingEntity)) continue;

			final ElementComponent component = ElementComponent.KEY.get(livingEntity);

			if (component.hasElementalApplication(Element.QUICKEN)) this.renderQuickenAura(context, livingEntity);
			else if (component.hasElementalApplication(Element.ELECTRO)) this.renderElectroAura(context, livingEntity);
		}
	}

	private void renderChargeLine(WorldRenderContext context, ChargeLinePositions clp, Entity entity, Color outerColor, Color innerColor) {
		final Vec3d initialPos = clp.getInitialPos(entity);

		this.renderChargeLine(context, initialPos, clp.generatePositions(this, entity), outerColor, innerColor);
	}

	@SuppressWarnings("unused")
	private void renderChargeLine(WorldRenderContext context, Vec3d initialPos, Vec3d finalPos, Color outerColor, Color innerColor) {
		final List<Vec3d> positions = this.generatePositions(Vec3d.ZERO, initialPos.subtract(finalPos));

		Vec3d randomVec = Vec3d.ZERO;

		for (int i = 0; i < positions.size(); i++) {
			randomVec = new Vec3d(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5);

			positions.set(i, positions.get(i).add(randomVec));
		}

		positions.add(0, Vec3d.ZERO);
		positions.add(finalPos.subtract(initialPos));

		this.renderChargeLine(context, initialPos, positions, outerColor, innerColor);
	}

	private void renderChargeLine(WorldRenderContext context, Vec3d origin, List<Vec3d> positions, Color outerColor, Color innerColor) {
	    final Camera camera = context.camera();
	    final Vec3d camPos = camera.getPos();

	    final MatrixStack matrices = new MatrixStack();
	    matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
	    matrices.translate(origin.x - camPos.x, origin.y - camPos.y, origin.z - camPos.z);

	    final Matrix4f posMat = matrices.peek().getPositionMatrix();
	    final MatrixStack.Entry entry = matrices.peek();

		final BufferBuilder outerLineBuffer = SevenElementsRenderer.createBuffer(allocator, SevenElementsRenderPipelines.CHARGE_LINE);

		for (int i = 1; i < positions.size(); i++) {
			final Vec3d start = positions.get(i - 1);
			final Vec3d end = positions.get(i);
			Vec3d normal = end.normalize();

		    outerLineBuffer.vertex(posMat, (float) start.x, (float) start.y, (float) start.z)
		       .color(outerColor.asARGB())
		       .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);

		    outerLineBuffer.vertex(posMat, (float) end.x, (float) end.y, (float) end.z)
		       .color(outerColor.asARGB())
		       .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
		}

		SevenElementsRenderLayer.getOuterChargeLine().draw(outerLineBuffer.end());

		final BufferBuilder innerLineBuffer = SevenElementsRenderer.createBuffer(allocator, SevenElementsRenderPipelines.CHARGE_LINE);

		for (int i = 1; i < positions.size(); i++) {
			final Vec3d start = positions.get(i - 1);
			final Vec3d end = positions.get(i);
			Vec3d normal = end.normalize();

		    innerLineBuffer.vertex(posMat, (float) start.x, (float) start.y, (float) start.z)
		       .color(innerColor.asARGB())
		       .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);

		    innerLineBuffer.vertex(posMat, (float) end.x, (float) end.y, (float) end.z)
		       .color(innerColor.asARGB())
		       .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
		}

		SevenElementsRenderLayer.getInnerChargeLine().draw(innerLineBuffer.end());

	    matrices.pop();
	}

	private List<Vec3d> generatePositions(final Vec3d initialPos, final Vec3d finalPos) {
		final Vec3d norm = initialPos.subtract(finalPos);
		final double length = norm.length();

		final int n = Math.max(1, this.poisson(SpecialEffectsRenderer.POISSON_DENSITY * length));
		final List<Double> doubles = new ArrayList<>();

		for (int i = 0; i < n; i++) doubles.add(RANDOM.nextDouble());

		return doubles
			.stream()
			.sorted()
			.map(t -> initialPos.add(norm.multiply(t)).add(new Vec3d(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5)))
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
			this.time = MinecraftClient.getInstance().world.getTime();
			this.mainEntity = mainEntity;
			this.otherEntities = otherEntities;
		}

		boolean shouldRemove() {
			return !mainEntity.isAlive() || otherEntities.isEmpty() || MinecraftClient.getInstance().world.getTime() > this.time + MAX_TICKS;
		}

		void render(final WorldRenderContext context, final SpecialEffectsRenderer renderer) {
			final double gradientStep = MathHelper.clamp(MathHelper.getLerpProgress(MinecraftClient.getInstance().world.getTime() - this.time + context.tickCounter().getTickProgress(false), 0, 10), 0, 1);
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

		private Vec3d entityPos(Entity entity) {
			return entity.getPos().add(0, entity.getHeight() * 0.5, 0);
		}
	}

	private static class StoredElectroChargedPositions {
		private final Entity mainEntity;
		private final Entity targetEntity;
		private Vec3d prevMainEntityPos;
		private Vec3d prevTargetEntityPos;
		private @Nullable List<Vec3d> positions = null;

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

		private List<Vec3d> generatePositions(SpecialEffectsRenderer renderer) {
			if (this.positions != null && shouldPositionsPersist()) return this.positions;

			// Required unequal due to shouldPositionsPersist(), refresh
			final Vec3d initialPos = this.prevMainEntityPos = this.entityPos(this.mainEntity);
			final Vec3d finalPos = this.prevTargetEntityPos = this.entityPos(this.targetEntity);

			final List<Vec3d> positions = renderer.generatePositions(Vec3d.ZERO, initialPos.subtract(finalPos));

			Vec3d randomVec = Vec3d.ZERO;

			for (int i = 0; i < positions.size(); i++) {
				randomVec = new Vec3d(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5);

				positions.set(i, positions.get(i).add(randomVec));
			}

			positions.add(0, Vec3d.ZERO);
			positions.add(finalPos.subtract(initialPos));

			this.positions = positions;

			return positions;
		}

		private Vec3d entityPos(Entity entity) {
			return entity.getPos().add(0, entity.getHeight() * 0.5, 0);
		}
	}

	private static class ChargeLinePositions {
		private final Vec3d initialPos;
		private final Vec3d finalPos;
		private @Nullable List<Vec3d> positions = null;
		private @Nullable Color color = null;

		private ChargeLinePositions(Vec3d initialPos, Vec3d finalPos, Entity relativeTo) {
			final Vec3d entityPos = relativeTo.getPos();

			this.initialPos = initialPos.subtract(entityPos);
			this.finalPos = finalPos.subtract(entityPos);
		}

		private Vec3d getInitialPos(Entity relativeTo) {
			return this.initialPos.add(relativeTo.getPos());
		}

		private List<Vec3d> generatePositions(SpecialEffectsRenderer renderer, Entity relativeTo) {
			if (this.positions != null) return this.positions;

			final Vec3d entityPos = relativeTo.getPos();
			final List<Vec3d> positions = renderer.generatePositions(Vec3d.ZERO, initialPos.subtract(finalPos));

			Vec3d randomVec = Vec3d.ZERO;

			for (int i = 0; i < positions.size(); i++) {
				randomVec = new Vec3d(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() - 0.5);

				positions.set(i, positions.get(i).add(randomVec));
			}

			positions.add(0, Vec3d.ZERO);
			positions.add(finalPos.subtract(initialPos));

			this.positions = positions;

			return positions
				.stream()
				.map(Functions.<Vec3d, Vec3d, Vec3d>withArgument(Vec3d::add, entityPos))
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
