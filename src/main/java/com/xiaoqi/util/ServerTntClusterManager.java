package com.xiaoqi.util;

import com.xiaoqi.CarpetVisualFixSettings;
import com.xiaoqi.network.TntBatchFusePayload;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.TntEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerTntClusterManager {
	private static final double MERGE_DISTANCE = 0.05;
	private static final double MERGE_DISTANCE_SQ = MERGE_DISTANCE * MERGE_DISTANCE; // 0.0025
	private static final double CELL_SIZE = 0.05;

	private static final Map<RegistryKey<World>, List<TntBatchFusePayload.TntBatchEntry>> LAST_LAZY_BATCHES = new ConcurrentHashMap<>();

	public static class Cluster {
		public double sumX;
		public double sumY;
		public double sumZ;
		public double firstX;
		public double firstY;
		public double firstZ;
		public int fuse;
		public int count;

		public Cluster(double x, double y, double z, int fuse) {
			this.firstX = x;
			this.firstY = y;
			this.firstZ = z;
			this.sumX = x;
			this.sumY = y;
			this.sumZ = z;
			this.fuse = fuse;
			this.count = 1;
		}

		public void add(double x, double y, double z) {
			this.sumX += x;
			this.sumY += y;
			this.sumZ += z;
			this.count++;
		}

		public double getCenterX() {
			return this.sumX / this.count;
		}

		public double getCenterY() {
			return this.sumY / this.count;
		}

		public double getCenterZ() {
			return this.sumZ / this.count;
		}
	}

	public static void tickWorld(ServerWorld world) {
		if (!CarpetVisualFixSettings.tntFuseLabel || !ServerF3BTracker.anyPlayerHasF3B()) {
			return;
		}

		List<ServerPlayerEntity> f3bPlayers = new ArrayList<>();
		List<ServerPlayerEntity> initialSyncPlayers = new ArrayList<>();

		for (ServerPlayerEntity player : world.getPlayers()) {
			if (ServerF3BTracker.hasF3B(player)) {
				f3bPlayers.add(player);
				if (ServerF3BTracker.pollInitialSync(player.getUuid())) {
					initialSyncPlayers.add(player);
				}
			}
		}

		if (f3bPlayers.isEmpty()) {
			return;
		}

		RegistryKey<World> dimensionKey = world.getRegistryKey();
		List<? extends TntEntity> tntEntities = world.getEntitiesByType(TypeFilter.instanceOf(TntEntity.class), tnt -> !tnt.isRemoved());

		if (tntEntities.isEmpty()) {
			List<TntBatchFusePayload.TntBatchEntry> previousLazy = LAST_LAZY_BATCHES.remove(dimensionKey);
			boolean hadLazy = previousLazy != null && !previousLazy.isEmpty();
			TntBatchFusePayload emptyPayload = new TntBatchFusePayload(List.of(), List.of(), hadLazy);
			for (ServerPlayerEntity player : f3bPlayers) {
				ServerPlayNetworking.send(player, emptyPayload);
			}
			return;
		}

		// Partition active and lazy TNTs
		List<TntEntity> activeTnts = new ArrayList<>();
		List<TntEntity> lazyTnts = new ArrayList<>();
		for (TntEntity tnt : tntEntities) {
			if (ServerLazyChunkHelper.isInLazyChunk(tnt)) {
				lazyTnts.add(tnt);
			} else {
				activeTnts.add(tnt);
			}
		}

		List<TntBatchFusePayload.TntBatchEntry> activeEntries = toEntries(clusterTnts(activeTnts));
		List<TntBatchFusePayload.TntBatchEntry> currentLazyEntries = toEntries(clusterTnts(lazyTnts));

		List<TntBatchFusePayload.TntBatchEntry> previousLazy = LAST_LAZY_BATCHES.get(dimensionKey);
		boolean lazyChanged = hasBatchesChanged(previousLazy, currentLazyEntries);

		if (lazyChanged) {
			LAST_LAZY_BATCHES.put(dimensionKey, currentLazyEntries);
		}

		TntBatchFusePayload standardPayload = new TntBatchFusePayload(
			activeEntries,
			lazyChanged ? currentLazyEntries : List.of(),
			lazyChanged
		);

		TntBatchFusePayload fullInitialPayload = null;
		if (!initialSyncPlayers.isEmpty()) {
			fullInitialPayload = new TntBatchFusePayload(activeEntries, currentLazyEntries, true);
		}

		for (ServerPlayerEntity player : f3bPlayers) {
			if (initialSyncPlayers.contains(player)) {
				ServerPlayNetworking.send(player, fullInitialPayload);
			} else {
				ServerPlayNetworking.send(player, standardPayload);
			}
		}
	}

	private static boolean hasBatchesChanged(List<TntBatchFusePayload.TntBatchEntry> oldList, List<TntBatchFusePayload.TntBatchEntry> newList) {
		if (oldList == null) {
			return !newList.isEmpty();
		}
		if (oldList.size() != newList.size()) {
			return true;
		}
		for (int i = 0; i < oldList.size(); i++) {
			TntBatchFusePayload.TntBatchEntry o = oldList.get(i);
			TntBatchFusePayload.TntBatchEntry n = newList.get(i);
			if (o.fuse() != n.fuse() || o.count() != n.count()) {
				return true;
			}
			double dx = o.x() - n.x();
			double dy = o.y() - n.y();
			double dz = o.z() - n.z();
			if (dx * dx + dy * dy + dz * dz > 1.0E-4) {
				return true;
			}
		}
		return false;
	}

	public static List<Cluster> clusterTnts(List<TntEntity> tnts) {
		if (tnts.isEmpty()) {
			return List.of();
		}

		// Group by fuse first
		Int2ObjectOpenHashMap<List<TntEntity>> byFuse = new Int2ObjectOpenHashMap<>();
		for (TntEntity tnt : tnts) {
			byFuse.computeIfAbsent(tnt.getFuse(), k -> new ArrayList<>()).add(tnt);
		}

		List<Cluster> result = new ArrayList<>();

		for (var entry : byFuse.int2ObjectEntrySet()) {
			int fuse = entry.getIntKey();
			List<TntEntity> list = entry.getValue();

			Long2ObjectOpenHashMap<List<Cluster>> grid = new Long2ObjectOpenHashMap<>();

			for (TntEntity tnt : list) {
				double x = tnt.getX();
				double y = tnt.getY();
				double z = tnt.getZ();

				long cx = (long) Math.floor(x / CELL_SIZE);
				long cy = (long) Math.floor(y / CELL_SIZE);
				long cz = (long) Math.floor(z / CELL_SIZE);

				Cluster matchedCluster = null;

				search:
				for (long dx = -1; dx <= 1; dx++) {
					for (long dy = -1; dy <= 1; dy++) {
						for (long dz = -1; dz <= 1; dz++) {
							long key = getCellKey(cx + dx, cy + dy, cz + dz);
							List<Cluster> cellClusters = grid.get(key);
							if (cellClusters != null) {
								for (Cluster c : cellClusters) {
									double diffX = x - c.firstX;
									double diffY = y - c.firstY;
									double diffZ = z - c.firstZ;
									if (diffX * diffX + diffY * diffY + diffZ * diffZ < MERGE_DISTANCE_SQ) {
										matchedCluster = c;
										break search;
									}
								}
							}
						}
					}
				}

				if (matchedCluster != null) {
					matchedCluster.add(x, y, z);
				} else {
					Cluster newCluster = new Cluster(x, y, z, fuse);
					result.add(newCluster);
					long cellKey = getCellKey(cx, cy, cz);
					grid.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(newCluster);
				}
			}
		}

		return result;
	}

	private static long getCellKey(long x, long y, long z) {
		return (x * 73856093L) ^ (y * 19349663L) ^ (z * 83492791L);
	}

	private static List<TntBatchFusePayload.TntBatchEntry> toEntries(List<Cluster> clusters) {
		List<TntBatchFusePayload.TntBatchEntry> entries = new ArrayList<>(clusters.size());
		for (Cluster c : clusters) {
			entries.add(new TntBatchFusePayload.TntBatchEntry(
				c.getCenterX(),
				c.getCenterY(),
				c.getCenterZ(),
				c.fuse,
				c.count
			));
		}
		return entries;
	}
}
