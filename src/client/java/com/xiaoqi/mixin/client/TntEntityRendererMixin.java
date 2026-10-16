package com.xiaoqi.mixin.client;

import com.xiaoqi.CarpetVisualFixSettings;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.client.render.entity.state.TntEntityRenderState;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(TntEntityRenderer.class)
public abstract class TntEntityRendererMixin {
	@Unique
	private static final float NEUTRAL_FUSE = 85.0F;

	@Unique
	private static final Map<TntEntity, Integer> LAST_FUSE = new WeakHashMap<>();

	@Inject(
		method = "updateRenderState(Lnet/minecraft/entity/TntEntity;Lnet/minecraft/client/render/entity/state/TntEntityRenderState;F)V",
		at = @At("TAIL")
	)
	private void onUpdateRenderState(TntEntity entity, TntEntityRenderState state, float tickProgress, CallbackInfo ci) {
		int fuse = entity.getFuse();

		if (CarpetVisualFixSettings.tntNoFlashScale) {
			state.fuse = NEUTRAL_FUSE;
			return;
		}

		Integer last = LAST_FUSE.get(entity);
		if (last != null && last.intValue() == fuse) {
			state.fuse = fuse + 1.0F;
		}
		LAST_FUSE.put(entity, fuse);
	}
}
