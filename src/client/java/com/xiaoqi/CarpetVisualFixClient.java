package com.xiaoqi;

import com.xiaoqi.network.C2SF3BStatePayload;
import com.xiaoqi.network.EntityLazyStatePayload;
import com.xiaoqi.network.SyncVisualFixPayload;
import com.xiaoqi.network.TntBatchFusePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;

public class CarpetVisualFixClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncVisualFixPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				boolean previousFuseLabel = CarpetVisualFixSettings.tntFuseLabel;
				CarpetVisualFixSettings.lazyEntityRenderingFix = payload.lazyEntityRenderingFix();
				CarpetVisualFixSettings.tntMotionArrow = payload.tntMotionArrow();
				CarpetVisualFixSettings.tntNoFlashScale = payload.tntNoFlashScale();
				CarpetVisualFixSettings.tntFuseLabel = payload.tntFuseLabel();
				CarpetVisualFixSettings.hideFacingArrow = payload.hideFacingArrow();

				if (!previousFuseLabel && payload.tntFuseLabel() && context.client().debugHudEntryList != null) {
					boolean isF3B = context.client().debugHudEntryList.isEntryVisible(DebugHudEntries.ENTITY_HITBOXES);
					if (isF3B && ClientPlayNetworking.canSend(C2SF3BStatePayload.ID)) {
						ClientPlayNetworking.send(new C2SF3BStatePayload(true));
					}
				}
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(EntityLazyStatePayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				LazyChunkHelper.setEntityLazy(payload.entityId(), payload.isLazy(), context.client().world);
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(TntBatchFusePayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				ClientTntClusterManager.updateClusters(payload);
			});
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (client.debugHudEntryList != null) {
				boolean visible = client.debugHudEntryList.isEntryVisible(DebugHudEntries.ENTITY_HITBOXES);
				if (ClientPlayNetworking.canSend(C2SF3BStatePayload.ID)) {
					ClientPlayNetworking.send(new C2SF3BStatePayload(visible));
				}
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			LazyChunkHelper.onDisconnected();
			ClientTntClusterManager.clear();
			CarpetVisualFixSettings.lazyEntityRenderingFix = true;
			CarpetVisualFixSettings.tntMotionArrow = false;
			CarpetVisualFixSettings.tntNoFlashScale = false;
			CarpetVisualFixSettings.tntFuseLabel = false;
			CarpetVisualFixSettings.hideFacingArrow = false;
		});
	}
}
