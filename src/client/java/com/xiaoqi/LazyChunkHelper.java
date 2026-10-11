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

public class LazyChunkHelper {
	private static final Int2BooleanOpenHashMap PENDING_STATES = new Int2BooleanOpenHashMap();

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
			return true;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null) {
			return false;
		}

		ChunkPos entityChunkPos = entity.getChunkPos();
		return isLazyChunk(entityChunkPos.x, entityChunkPos.z);
	}
	
	private static boolean canPlayerLoadChunks(ClientPlayerEntity player) {
		if (player.isCreative() && !CarpetSettings.creativePlayersLoadChunks) {
			return false;
		}
		return true;
	}
}
