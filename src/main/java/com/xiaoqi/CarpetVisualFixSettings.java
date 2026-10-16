package com.xiaoqi;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;
import carpet.api.settings.Validator;
import com.xiaoqi.network.SyncVisualFixPayload;
import com.xiaoqi.util.ServerF3BTracker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CarpetVisualFixSettings {
	@Rule(
		categories = {RuleCategory.CLIENT},
		validators = {SettingsSyncValidator.class}
	)
	public static boolean lazyEntityRenderingFix = true;

	@Rule(
		categories = {RuleCategory.CLIENT},
		validators = {SettingsSyncValidator.class}
	)
	public static boolean tntMotionArrow = false;

	@Rule(
		categories = {RuleCategory.CLIENT},
		validators = {SettingsSyncValidator.class}
	)
	public static boolean tntNoFlashScale = false;

	@Rule(
		categories = {RuleCategory.CLIENT},
		validators = {SettingsSyncValidator.class}
	)
	public static boolean tntFuseLabel = false;

	@Rule(
		categories = {RuleCategory.CLIENT},
		validators = {SettingsSyncValidator.class}
	)
	public static boolean hideFacingArrow = false;

	public static boolean isFixEnabled() {
		return lazyEntityRenderingFix;
	}

	public static void syncToAll(MinecraftServer server) {
		if (server == null) return;
		SyncVisualFixPayload payload = createPayload();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	public static void syncToPlayer(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, createPayload());
	}

	private static SyncVisualFixPayload createPayload() {
		return new SyncVisualFixPayload(
			lazyEntityRenderingFix,
			tntMotionArrow,
			tntNoFlashScale,
			tntFuseLabel,
			hideFacingArrow
		);
	}

	public static class SettingsSyncValidator extends Validator<Boolean> {
		@Override
		public Boolean validate(ServerCommandSource source, CarpetRule<Boolean> currentRule, Boolean newValue, String string) {
			if (currentRule != null && "tntFuseLabel".equals(currentRule.name()) && Boolean.TRUE.equals(newValue)) {
				ServerF3BTracker.requestInitialSyncForAll();
			}
			if (source != null && source.getServer() != null) {
				source.getServer().execute(() -> {
					syncToAll(source.getServer());
				});
			}
			return newValue;
		}
	}
}
