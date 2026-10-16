package com.xiaoqi.mixin;

import com.xiaoqi.CarpetVisualFixSettings;
import com.xiaoqi.network.EntityLazyStatePayload;
import com.xiaoqi.util.ServerF3BTracker;
import com.xiaoqi.util.ServerLazyChunkHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {
	@Shadow @Final private Entity entity;
	@Shadow @Final private ServerWorld world;
	@Shadow @Final private EntityTrackerEntry.TrackerPacketSender packetSender;

	@Unique private double lastTrackedX;
	@Unique private double lastTrackedY;
	@Unique private double lastTrackedZ;
	@Unique private boolean coordsInitialized = false;
	@Unique private boolean lastLazyState = false;

	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(ServerWorld world, Entity entity, int tickInterval, boolean alwaysUpdateVelocity, EntityTrackerEntry.TrackerPacketSender packetSender, CallbackInfo ci) {
		this.lastTrackedX = entity.getX();
		this.lastTrackedY = entity.getY();
		this.lastTrackedZ = entity.getZ();
		this.coordsInitialized = true;
		this.lastLazyState = CarpetVisualFixSettings.isFixEnabled() && isInLazyChunk();
	}

	@Inject(
		method = "sendPackets",
		at = @At("TAIL")
	)
	private void onSendPackets(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> sender, CallbackInfo ci) {
		if (CarpetVisualFixSettings.isFixEnabled() && isInLazyChunk()) {
			ServerPlayNetworking.send(player, new EntityLazyStatePayload(this.entity.getId(), true));
		}
	}

	@Inject(
		method = "tick",
		at = @At("TAIL")
	)
	private void onTick(CallbackInfo ci) {
		if (!this.coordsInitialized) {
			this.lastTrackedX = this.entity.getX();
			this.lastTrackedY = this.entity.getY();
			this.lastTrackedZ = this.entity.getZ();
			this.coordsInitialized = true;
			this.lastLazyState = CarpetVisualFixSettings.isFixEnabled() && isInLazyChunk();
			return;
		}

		boolean isLazy = CarpetVisualFixSettings.isFixEnabled() && isInLazyChunk();
		if (isLazy != this.lastLazyState) {
			this.lastLazyState = isLazy;
			this.packetSender.sendToListeners(ServerPlayNetworking.createS2CPacket(new EntityLazyStatePayload(this.entity.getId(), isLazy)));
		}

		if (!isLazy) {
			this.lastTrackedX = this.entity.getX();
			this.lastTrackedY = this.entity.getY();
			this.lastTrackedZ = this.entity.getZ();
			return;
		}

		boolean movedThisTick = this.entity.getX() != this.lastTrackedX
			|| this.entity.getY() != this.lastTrackedY
			|| this.entity.getZ() != this.lastTrackedZ;

		if (movedThisTick) {
			this.packetSender.sendToListeners(EntityPositionSyncS2CPacket.create(this.entity));
			this.lastTrackedX = this.entity.getX();
			this.lastTrackedY = this.entity.getY();
			this.lastTrackedZ = this.entity.getZ();
			this.entity.velocityDirty = false;
		} else {
			this.entity.velocityDirty = false;
		}
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/EntityTrackerEntry$TrackerPacketSender;sendToListeners(Lnet/minecraft/network/packet/Packet;)V"
		)
	)
	private void onSendToListeners(EntityTrackerEntry.TrackerPacketSender sender, Packet<? super ClientPlayPacketListener> packet) {
		if (CarpetVisualFixSettings.isFixEnabled() && isInLazyChunk()) {
			// Suppress relative delta movement and velocity updates that cause jitter,
			// while allowing absolute position sync (EntityPositionSyncS2CPacket) and metadata to pass through.
			if (packet instanceof EntityS2CPacket || packet instanceof EntityVelocityUpdateS2CPacket) {
				return;
			}
		}
		sender.sendToListeners(packet);
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/EntityTrackerEntry$TrackerPacketSender;sendToSelfAndListeners(Lnet/minecraft/network/packet/Packet;)V"
		)
	)
	private void onSendToSelfAndListeners(EntityTrackerEntry.TrackerPacketSender sender, Packet<? super ClientPlayPacketListener> packet) {
		if (CarpetVisualFixSettings.isFixEnabled() && isInLazyChunk()) {
			if (packet instanceof EntityVelocityUpdateS2CPacket) {
				return;
			}
		}
		if (this.entity instanceof net.minecraft.entity.TntEntity && (CarpetVisualFixSettings.isFixEnabled() || CarpetVisualFixSettings.tntFuseLabel)) {
			if (packet instanceof net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket) {
				sender.sendToListenersIf(packet, player -> !ServerF3BTracker.hasF3B(player));
				return;
			}
		}
		sender.sendToSelfAndListeners(packet);
	}

	private boolean isInLazyChunk() {
		return ServerLazyChunkHelper.isInLazyChunk(this.world, this.entity.getChunkPos());
	}
}
