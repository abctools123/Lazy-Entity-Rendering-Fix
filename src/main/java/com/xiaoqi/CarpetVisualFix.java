package com.xiaoqi;

import java.util.Map;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.xiaoqi.network.C2SF3BStatePayload;
import com.xiaoqi.network.EntityLazyStatePayload;
import com.xiaoqi.network.SyncVisualFixPayload;
import com.xiaoqi.network.TntBatchFusePayload;
import com.xiaoqi.util.ServerF3BTracker;
import com.xiaoqi.util.ServerTntClusterManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class CarpetVisualFix implements ModInitializer, CarpetExtension {
	public static final String MOD_ID = "carpet-visualfix";
	public static final String VERSION = "1.2.0";

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(SyncVisualFixPayload.ID, SyncVisualFixPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(EntityLazyStatePayload.ID, EntityLazyStatePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(TntBatchFusePayload.ID, TntBatchFusePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(C2SF3BStatePayload.ID, C2SF3BStatePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(C2SF3BStatePayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> {
				ServerF3BTracker.setF3B(player, payload.enabled());
			});
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			CarpetVisualFixSettings.syncToPlayer(player);
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerF3BTracker.onPlayerDisconnect(handler.player.getUuid());
		});

		ServerTickEvents.END_WORLD_TICK.register(ServerTntClusterManager::tickWorld);

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
				"carpet.rule.lazyEntityRenderingFix.desc", "修复了弱加载区域内实体的渲染",
				"carpet.rule.tntMotionArrow.desc", "在 F3+B 模式下绘制 TNT 的动量箭头",
				"carpet.rule.tntNoFlashScale.desc", "禁用 TNT 爆炸前的白闪和缩放效果",
				"carpet.rule.tntFuseLabel.desc", "显示 TNT 爆炸前的剩余刻数标签（自动合并 0.05 格内的同引信 TNT）",
				"carpet.rule.hideFacingArrow.desc", "在 F3+B 模式下隐藏实体的朝向箭头"
			);
		}
		return Map.of(
			"carpet.category.client", "client",
			"carpet.rule.lazyEntityRenderingFix.desc", "Fixes entity rendering inside lazy chunks",
			"carpet.rule.tntMotionArrow.desc", "Draws momentum arrow for primed TNT when F3+B is enabled",
			"carpet.rule.tntNoFlashScale.desc", "Disables TNT white flashing and scaling visual effect before exploding",
			"carpet.rule.tntFuseLabel.desc", "Shows a label with remaining ticks before TNT explodes (batches TNT within 0.05 blocks)",
			"carpet.rule.hideFacingArrow.desc", "Hides entity facing direction arrows in F3+B mode"
		);
	}
}
