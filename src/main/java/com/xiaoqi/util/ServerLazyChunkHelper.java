package com.xiaoqi.util;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

public class ServerLazyChunkHelper {
	/**
	 * Determines if a chunk at the given ChunkPos in ServerWorld is lazy (non-entity-ticking, but accessible/loaded).
	 * In Minecraft 1.21 ticket levels:
	 * Level <= 31: Entity Ticking (Active)
	 * Level == 32: Block Ticking (Lazy chunk: blocks and redstone tick, entities do NOT tick)
	 * Level == 33: Full / Border (Inaccessible for ticks, but loaded)
	 * Level > 33: Inaccessible / Unloaded
	 */
	public static boolean isInLazyChunk(ServerWorld world, ChunkPos chunkPos) {
		ChunkHolder chunkHolder = world.getChunkManager().chunkLoadingManager.getCurrentChunkHolder(chunkPos.toLong());
		if (chunkHolder == null) {
			return false;
		}
		int level = chunkHolder.getLevel();
		return !ChunkLevels.shouldTickEntities(level) && ChunkLevels.isAccessible(level);
	}

	public static boolean isInLazyChunk(Entity entity) {
		if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
			return isInLazyChunk(serverWorld, entity.getChunkPos());
		}
		return false;
	}
}
