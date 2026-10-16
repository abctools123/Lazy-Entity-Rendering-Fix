package com.xiaoqi.mixin.client;

import com.xiaoqi.network.C2SF3BStatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudEntryVisibility;
import net.minecraft.client.gui.hud.debug.DebugHudProfile;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DebugHudProfile.class)
public abstract class DebugHudProfileMixin {
	@Shadow public abstract boolean isEntryVisible(Identifier id);

	@Inject(method = "toggleVisibility", at = @At("RETURN"))
	private void onToggleVisibility(Identifier id, CallbackInfoReturnable<Boolean> cir) {
		if (DebugHudEntries.ENTITY_HITBOXES.equals(id)) {
			boolean visible = cir.getReturnValueZ();
			if (ClientPlayNetworking.canSend(C2SF3BStatePayload.ID)) {
				ClientPlayNetworking.send(new C2SF3BStatePayload(visible));
			}
		}
	}

	@Inject(method = "setEntryVisibility", at = @At("RETURN"))
	private void onSetEntryVisibility(Identifier id, DebugHudEntryVisibility visibility, CallbackInfo ci) {
		if (DebugHudEntries.ENTITY_HITBOXES.equals(id)) {
			boolean visible = isEntryVisible(id);
			if (ClientPlayNetworking.canSend(C2SF3BStatePayload.ID)) {
				ClientPlayNetworking.send(new C2SF3BStatePayload(visible));
			}
		}
	}
}
