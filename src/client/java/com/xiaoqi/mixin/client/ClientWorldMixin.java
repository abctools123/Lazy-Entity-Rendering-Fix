package com.xiaoqi.mixin.client;

import com.xiaoqi.CarpetVisualFixSettings;
import com.xiaoqi.LazyChunkHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
	@Inject(
		method = "tickEntity",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onTickEntity(Entity entity, CallbackInfo ci) {
		if (entity instanceof PlayerEntity) {
			return;
		}
		
		if (!CarpetVisualFixSettings.isFixEnabled()) {
			return;
		}
		
		if (LazyChunkHelper.isInLazyChunk(entity)) {
			entity.setVelocity(Vec3d.ZERO);

			if (entity.getInterpolator() != null) {
				entity.getInterpolator().clear();
			}

			ci.cancel();
		}
	}

	@Inject(
		method = "tickPassenger",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onTickPassenger(Entity entity, Entity passenger, CallbackInfo ci) {
		if (passenger instanceof PlayerEntity) {
			return;
		}

		if (!CarpetVisualFixSettings.isFixEnabled()) {
			return;
		}

		if (LazyChunkHelper.isInLazyChunk(passenger)) {
			passenger.setVelocity(Vec3d.ZERO);

			if (passenger.getInterpolator() != null) {
				passenger.getInterpolator().clear();
			}

			ci.cancel();
		}
	}
}
