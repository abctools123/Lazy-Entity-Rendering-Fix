package com.xiaoqi;

import com.xiaoqi.network.EntityLazyStatePayload;
import com.xiaoqi.network.SyncVisualFixPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetVisualFixClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("VisualFix-Client");

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncVisualFixPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				CarpetVisualFixSettings.lazyEntityRenderingFix = payload.enabled();
				LOGGER.info("[VisualFix-Client] Received sync from server: fixEnabled = {}", payload.enabled());
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(EntityLazyStatePayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				LazyChunkHelper.setEntityLazy(payload.entityId(), payload.isLazy(), context.client().world);
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			LazyChunkHelper.onDisconnected();
			CarpetVisualFixSettings.lazyEntityRenderingFix = true;
		});
	}
}
