package com.xiaoqi;

import carpet.CarpetSettings;
import com.xiaoqi.ducks.LazyEntityTracker;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LazyChunkHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger("VisualFix-Client");
	private static final Map<Integer, Long> LAST_LOGGED = new ConcurrentHashMap<>();
	private static final Int2BooleanOpenHashMap PENDING_STATES = new Int2BooleanOpenHashMap();
	private static final long LOG_INTERVAL_MS = 10000L;

	public static synchronized void setEntityLazy(int entityId, boolean isLazy, ClientWorld world) {
		if (world != null) {
			Entity entity = world.getEntityById(entityId);
			if (entity instanceof LazyEntityTracker tracker) {
				tracker.carpetVisualFix$setLazy(isLazy);
				return;
			}
		}
		PENDING_STATES.put(entityId, isLazy);
	}

	public static synchronized void onDisconnected() {
		PENDING_STATES.clear();
		LAST_LOGGED.clear();
	}

	private static void logFreezing(Entity entity, String source) {
		long now = System.currentTimeMillis();
		Long lastLog = LAST_LOGGED.get(entity.getId());
		if (lastLog == null || (now - lastLog) > LOG_INTERVAL_MS) {
			LAST_LOGGED.put(entity.getId(), now);
			LOGGER.info("[VisualFix-Client] Freezing entity {} at {} (Lazy status confirmed by {})",
				entity.getName().getString(),
				entity.getBlockPos().toShortString(),
				source);
			if (LAST_LOGGED.size() > 500) {
				LAST_LOGGED.entrySet().removeIf(e -> (now - e.getValue()) > LOG_INTERVAL_MS * 2);
			}
		}
	}

	public static boolean isLazyChunk(int chunkX, int chunkZ) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientWorld world = client.world;
		ClientPlayerEntity player = client.player;
		
		if (world == null || player == null) {
			return false;
		}
		
		ChunkPos playerChunkPos = player.getChunkPos();
		int simulationDistance = client.options.getSimulationDistance().getValue();
		
		int dx = Math.abs(chunkX - playerChunkPos.x);
		int dz = Math.abs(chunkZ - playerChunkPos.z);
		
		boolean inSimulationDistance = dx <= simulationDistance && dz <= simulationDistance;
		WorldChunk chunk = world.getChunkManager().getWorldChunk(chunkX, chunkZ);
		boolean chunkLoaded = chunk != null;
		
		boolean playerCanLoadChunks = canPlayerLoadChunks(player);
		return (!inSimulationDistance || !playerCanLoadChunks) && chunkLoaded;
	}

	public static boolean isInLazyChunk(Entity entity) {
		synchronized (LazyChunkHelper.class) {
			if (PENDING_STATES.containsKey(entity.getId())) {
				boolean pending = PENDING_STATES.remove(entity.getId());
				if (entity instanceof LazyEntityTracker tracker) {
					tracker.carpetVisualFix$setLazy(pending);
				}
			}
		}

		if (entity instanceof LazyEntityTracker tracker && tracker.carpetVisualFix$isLazy()) {
			if (CarpetVisualFixSettings.lazyEntityRenderingFix) {
				logFreezing(entity, "ServerSync");
			}
			return true;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null) {
			return false;
		}

		ChunkPos entityChunkPos = entity.getChunkPos();
		boolean inLazy = isLazyChunk(entityChunkPos.x, entityChunkPos.z);

		if (inLazy && CarpetVisualFixSettings.lazyEntityRenderingFix) {
			logFreezing(entity, "fallback");
		}

		return inLazy;
	}
	
	private static boolean canPlayerLoadChunks(ClientPlayerEntity player) {
		if (player.isCreative() && !CarpetSettings.creativePlayersLoadChunks) {
			return false;
		}
		return true;
	}
}
