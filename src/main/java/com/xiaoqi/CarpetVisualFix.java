package com.xiaoqi;

import java.util.Map;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.xiaoqi.network.EntityLazyStatePayload;
import com.xiaoqi.network.SyncVisualFixPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class CarpetVisualFix implements ModInitializer, CarpetExtension {
	public static final String MOD_ID = "carpet-visualfix";
	public static final String VERSION = "1.1.0";

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(SyncVisualFixPayload.ID, SyncVisualFixPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(EntityLazyStatePayload.ID, EntityLazyStatePayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			ServerPlayNetworking.send(player, new SyncVisualFixPayload(CarpetVisualFixSettings.isFixEnabled()));
			CarpetVisualFixSettings.sendPlayerNotifications(player);
		});

		CarpetServer.manageExtension(this);
	}

	@Override
	public void onGameStarted() {
		CarpetServer.settingsManager.parseSettingsClass(CarpetVisualFixSettings.class);
	}

	@Override
	public String version() {
		return MOD_ID;
	}

	@Override
	public Map<String, String> canHasTranslations(String lang) {
		if ("zh_cn".equals(lang)) {
			return Map.of(
				"carpet.category.client", "客户端",
				"carpet.rule.lazyEntityRenderingFix.desc", "修复了弱加载区域内实体的渲染"
			);
		}
		return Map.of(
			"carpet.category.client", "client",
			"carpet.rule.lazyEntityRenderingFix.desc", "Fixes entity rendering inside lazy chunks"
		);
	}
}
