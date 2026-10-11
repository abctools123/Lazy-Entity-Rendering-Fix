package com.xiaoqi.mixin.client;

import com.xiaoqi.CarpetVisualFixSettings;
import com.xiaoqi.LazyChunkHelper;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void onTick(CallbackInfo ci) {
		TntEntity tnt = (TntEntity) (Object) this;
		if (tnt.getEntityWorld().isClient() && CarpetVisualFixSettings.isFixEnabled() && LazyChunkHelper.isInLazyChunk(tnt)) {
			tnt.setVelocity(Vec3d.ZERO);
			ci.cancel();
		}
	}
}
