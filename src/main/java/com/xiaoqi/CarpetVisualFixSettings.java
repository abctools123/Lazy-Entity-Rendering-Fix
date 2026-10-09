package com.xiaoqi;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;
import carpet.api.settings.Validator;
import com.xiaoqi.network.SyncVisualFixPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CarpetVisualFixSettings {
	@Rule(
		categories = {RuleCategory.CLIENT},
		validators = {LazyEntityRenderingFixValidator.class}
	)
	public static boolean lazyEntityRenderingFix = true;

	public static class LazyEntityRenderingFixValidator extends Validator<Boolean> {
		@Override
		public Boolean validate(ServerCommandSource source, CarpetRule<Boolean> currentRule, Boolean newValue, String string) {
			lazyEntityRenderingFix = newValue;
			if (source != null && source.getServer() != null) {
				MinecraftServer server = source.getServer();
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					ServerPlayNetworking.send(player, new SyncVisualFixPayload(newValue));
				}
				sendServerNotifications(server, newValue);
			}
			return newValue;
		}
	}

	public static void sendPlayerNotifications(ServerPlayerEntity player) {
		if (lazyEntityRenderingFix) {
			player.sendMessage(Text.literal("§a[LazyEntityRenderingFix] Active: Lazy entity rendering fix enabled."), false);
		}
	}

	public static void sendServerNotifications(MinecraftServer server, boolean enabled) {
		if (enabled) {
			server.getPlayerManager().broadcast(Text.literal("§a[LazyEntityRenderingFix] Active: Lazy entity rendering fix enabled."), false);
		}
	}
}
