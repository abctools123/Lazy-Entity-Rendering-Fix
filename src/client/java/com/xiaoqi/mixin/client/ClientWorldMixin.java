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
		
		if (!CarpetVisualFixSettings.lazyEntityRenderingFix) {
			return;
		}
		
		if (LazyChunkHelper.isInLazyChunk(entity)) {
			entity.setVelocity(Vec3d.ZERO);
			entity.lastX = entity.getX();
			entity.lastY = entity.getY();
			entity.lastZ = entity.getZ();
			entity.lastRenderX = entity.getX();
			entity.lastRenderY = entity.getY();
			entity.lastRenderZ = entity.getZ();
			entity.resetPosition();

			if (entity.getInterpolator() != null) {
				entity.getInterpolator().clear();
			}

			if (entity instanceof TntEntity tnt) {
				tnt.setFuse(tnt.getFuse());
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

		if (!CarpetVisualFixSettings.lazyEntityRenderingFix) {
			return;
		}

		if (LazyChunkHelper.isInLazyChunk(passenger)) {
			passenger.setVelocity(Vec3d.ZERO);
			passenger.lastX = passenger.getX();
			passenger.lastY = passenger.getY();
			passenger.lastZ = passenger.getZ();
			passenger.lastRenderX = passenger.getX();
			passenger.lastRenderY = passenger.getY();
			passenger.lastRenderZ = passenger.getZ();
			passenger.resetPosition();

			if (passenger.getInterpolator() != null) {
				passenger.getInterpolator().clear();
			}

			ci.cancel();
		}
	}
}
