package com.xiaoqi.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerF3BTracker {
	private static final Set<UUID> PLAYERS_WITH_F3B = Collections.synchronizedSet(new HashSet<>());
	private static final Set<UUID> PENDING_INITIAL_SYNC = Collections.synchronizedSet(new HashSet<>());

	public static void setF3B(ServerPlayerEntity player, boolean enabled) {
		setF3B(player.getUuid(), enabled);
	}

	public static void setF3B(UUID uuid, boolean enabled) {
		if (enabled) {
			PLAYERS_WITH_F3B.add(uuid);
			PENDING_INITIAL_SYNC.add(uuid);
		} else {
			PLAYERS_WITH_F3B.remove(uuid);
			PENDING_INITIAL_SYNC.remove(uuid);
		}
	}

	public static void onPlayerDisconnect(UUID uuid) {
		PLAYERS_WITH_F3B.remove(uuid);
		PENDING_INITIAL_SYNC.remove(uuid);
	}

	public static boolean hasF3B(ServerPlayerEntity player) {
		return hasF3B(player.getUuid());
	}

	public static boolean hasF3B(UUID uuid) {
		return PLAYERS_WITH_F3B.contains(uuid);
	}

	public static boolean anyPlayerHasF3B() {
		return !PLAYERS_WITH_F3B.isEmpty();
	}

	public static boolean pollInitialSync(UUID uuid) {
		return PENDING_INITIAL_SYNC.remove(uuid);
	}

	public static void requestInitialSyncForAll() {
		PENDING_INITIAL_SYNC.addAll(PLAYERS_WITH_F3B);
	}
}
