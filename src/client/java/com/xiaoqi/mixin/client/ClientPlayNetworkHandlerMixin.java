package com.xiaoqi.mixin.client;

import com.xiaoqi.CarpetVisualFixSettings;
import com.xiaoqi.LazyChunkHelper;
import com.xiaoqi.ducks.LazyEntityTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Shadow private ClientWorld world;

	@Inject(
		method = "onEntityVelocityUpdate",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;setVelocityClient(Lnet/minecraft/util/math/Vec3d;)V"
		),
		cancellable = true
	)
	private void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
		if (!CarpetVisualFixSettings.isFixEnabled() || this.world == null) {
			return;
		}

		Entity entity = this.world.getEntityById(packet.getEntityId());
		if (entity != null && !(entity instanceof PlayerEntity) && isLazy(entity)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "onEntityPositionSync",
		at = @At("TAIL")
	)
	private void onEntityPositionSync(EntityPositionSyncS2CPacket packet, CallbackInfo ci) {
		if (!CarpetVisualFixSettings.isFixEnabled() || this.world == null) {
			return;
		}

		Entity entity = this.world.getEntityById(packet.id());
		if (entity != null && !(entity instanceof PlayerEntity)) {
			Vec3d pos = packet.values().position();
			ChunkPos targetChunk = new ChunkPos(BlockPos.ofFloored(pos));
			if (isLazy(entity) || LazyChunkHelper.isLazyChunk(targetChunk.x, targetChunk.z)) {
				snapEntity(entity, pos, packet.values().yaw(), packet.values().pitch());
			}
		}
	}

	@Inject(
		method = "onEntityPosition",
		at = @At("TAIL")
	)
	private void onEntityPosition(EntityPositionS2CPacket packet, CallbackInfo ci) {
		if (!CarpetVisualFixSettings.isFixEnabled() || this.world == null) {
			return;
		}

		Entity entity = this.world.getEntityById(packet.entityId());
		if (entity != null && !(entity instanceof PlayerEntity)) {
			Vec3d targetPos = packet.relatives().isEmpty() ? packet.change().position() : entity.getEntityPos();
			ChunkPos targetChunk = new ChunkPos(BlockPos.ofFloored(targetPos));
			if (isLazy(entity) || LazyChunkHelper.isLazyChunk(targetChunk.x, targetChunk.z)) {
				float targetYaw = packet.relatives().contains(PositionFlag.Y_ROT) ? entity.getYaw() : packet.change().yaw();
				float targetPitch = packet.relatives().contains(PositionFlag.X_ROT) ? entity.getPitch() : packet.change().pitch();
				snapEntity(entity, targetPos, targetYaw, targetPitch);
			}
		}
	}

	@Unique
	private boolean isLazy(Entity entity) {
		if (entity instanceof LazyEntityTracker tracker && tracker.carpetVisualFix$isLazy()) {
			return true;
		}
		return LazyChunkHelper.isInLazyChunk(entity);
	}

	@Unique
	private void snapEntity(Entity entity, Vec3d pos, float yaw, float pitch) {
		entity.setPosition(pos);
		entity.setYaw(yaw);
		entity.setPitch(pitch);
		entity.resetPosition();
		if (entity.getInterpolator() != null) {
			entity.getInterpolator().clear();
		}
	}
}
