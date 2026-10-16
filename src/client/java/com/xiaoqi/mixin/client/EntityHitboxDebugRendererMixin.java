package com.xiaoqi.mixin.client;

import com.xiaoqi.CarpetVisualFixSettings;
import com.xiaoqi.ClientTntClusterManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class EntityHitboxDebugRendererMixin {
	@Shadow @Final MinecraftClient client;

	@Shadow
	private Entity getLocalServerEntity(Entity entity) {
		return null;
	}

	@Redirect(
		method = "drawHitbox",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/debug/gizmo/GizmoDrawing;arrow(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;I)Lnet/minecraft/world/debug/gizmo/VisibilityConfigurable;",
			ordinal = 0
		)
	)
	private VisibilityConfigurable onDrawFacingArrow(Vec3d start, Vec3d end, int color) {
		if (CarpetVisualFixSettings.hideFacingArrow) {
			return null;
		}
		return GizmoDrawing.arrow(start, end, color);
	}

	@Inject(
		method = "drawHitbox",
		at = @At("TAIL")
	)
	private void onDrawHitboxTail(Entity entity, float tickProgress, boolean serverSide, CallbackInfo ci) {
		if (!serverSide && CarpetVisualFixSettings.tntMotionArrow && entity instanceof TntEntity tnt) {
			Entity target = tnt;
			if (this.client.getServer() != null) {
				Entity serverEntity = getLocalServerEntity(tnt);
				if (serverEntity != null) {
					target = serverEntity;
				}
			}

			Vec3d velocity = target.getVelocity();
			if (velocity.lengthSquared() >= 1.0E-8) {
				Vec3d start = tnt.getLerpedPos(tickProgress);
				Vec3d end = start.add(velocity);
				GizmoDrawing.arrow(start, end, 0xFFFFFF00);
			}
		}
	}

	@Inject(
		method = "render",
		at = @At("TAIL")
	)
	private void onRenderTail(double cameraX, double cameraY, double cameraZ, DebugDataStore dataStore, Frustum frustum, float tickProgress, CallbackInfo ci) {
		if (CarpetVisualFixSettings.tntFuseLabel) {
			ClientTntClusterManager.renderClusters(cameraX, cameraY, cameraZ, frustum);
		}
	}
}
