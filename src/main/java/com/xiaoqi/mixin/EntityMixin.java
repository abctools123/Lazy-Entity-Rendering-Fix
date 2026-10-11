package com.xiaoqi.mixin;

import com.xiaoqi.CarpetVisualFixSettings;
import com.xiaoqi.ducks.LazyEntityTracker;
import com.xiaoqi.util.ServerLazyChunkHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements LazyEntityTracker {
	@Shadow public boolean velocityDirty;

	@Unique
	private boolean carpetVisualFix$isLazy = false;

	@Override
	public boolean carpetVisualFix$isLazy() {
		return this.carpetVisualFix$isLazy;
	}

	@Override
	public void carpetVisualFix$setLazy(boolean lazy) {
		this.carpetVisualFix$isLazy = lazy;
	}

	@Inject(
		method = "move",
		at = @At("TAIL")
	)
	private void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
		if (CarpetVisualFixSettings.isFixEnabled()) {
			Entity entity = (Entity) (Object) this;
			if (ServerLazyChunkHelper.isInLazyChunk(entity) && movement.lengthSquared() > 1.0E-5) {
				this.velocityDirty = true;
			}
		}
	}
}
