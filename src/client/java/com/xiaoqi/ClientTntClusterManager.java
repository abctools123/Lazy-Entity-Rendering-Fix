package com.xiaoqi;

import com.xiaoqi.network.TntBatchFusePayload;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.TextGizmo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClientTntClusterManager {
	private static final double MAX_RENDER_DIST_SQ = 64.0 * 64.0;
	private static final double COINCIDENT_DIST_SQ = 0.05 * 0.05; // 0.0025

	private static List<TntBatchFusePayload.TntBatchEntry> activeClusters = Collections.emptyList();
	private static List<TntBatchFusePayload.TntBatchEntry> lazyClusters = Collections.emptyList();

	public static void updateClusters(TntBatchFusePayload payload) {
		activeClusters = new ArrayList<>(payload.activeBatches());
		if (payload.hasLazyUpdate()) {
			lazyClusters = new ArrayList<>(payload.lazyBatches());
		}
	}

	public static void clear() {
		activeClusters = Collections.emptyList();
		lazyClusters = Collections.emptyList();
	}

	public static void renderClusters(double cameraX, double cameraY, double cameraZ, Frustum frustum) {
		List<TntBatchFusePayload.TntBatchEntry> allEntries = new ArrayList<>(activeClusters.size() + lazyClusters.size());
		allEntries.addAll(activeClusters);
		allEntries.addAll(lazyClusters);

		if (allEntries.isEmpty()) {
			return;
		}

		// Group coincident entries to prevent overlapping text (Bug 3)
		List<List<TntBatchFusePayload.TntBatchEntry>> groups = new ArrayList<>();
		for (TntBatchFusePayload.TntBatchEntry entry : allEntries) {
			double dx = entry.x() - cameraX;
			double dy = entry.y() - cameraY;
			double dz = entry.z() - cameraZ;
			double distSq = dx * dx + dy * dy + dz * dz;

			if (distSq > MAX_RENDER_DIST_SQ) {
				continue;
			}

			boolean placed = false;
			for (List<TntBatchFusePayload.TntBatchEntry> group : groups) {
				TntBatchFusePayload.TntBatchEntry rep = group.get(0);
				double gdx = entry.x() - rep.x();
				double gdy = entry.y() - rep.y();
				double gdz = entry.z() - rep.z();
				if (gdx * gdx + gdz * gdz < COINCIDENT_DIST_SQ && Math.abs(gdy) < 0.5) {
					group.add(entry);
					placed = true;
					break;
				}
			}
			if (!placed) {
				List<TntBatchFusePayload.TntBatchEntry> newGroup = new ArrayList<>();
				newGroup.add(entry);
				groups.add(newGroup);
			}
		}

		// Render each group, vertically stacking if coincident
		for (List<TntBatchFusePayload.TntBatchEntry> group : groups) {
			// Sort ascending by fuse (lowest fuse at bottom, highest at top)
			group.sort(Comparator.comparingInt(TntBatchFusePayload.TntBatchEntry::fuse));

			for (int i = 0; i < group.size(); i++) {
				TntBatchFusePayload.TntBatchEntry entry = group.get(i);
				double yOffset = 1.5 + (i * 0.35);
				Vec3d labelPos = new Vec3d(entry.x(), entry.y() + yOffset, entry.z());

				int color = colorForFuse(entry.fuse());
				String text = entry.fuse() + "t" + (entry.count() > 1 ? " (" + entry.count() + "x)" : "");

				GizmoDrawing.text(text, labelPos, TextGizmo.Style.centered(color));
			}
		}
	}

	public static int colorForFuse(int fuse) {
		if (fuse < 20) return 0xFFFF0000;
		if (fuse < 40) return 0xFFFFA500;
		return 0xFFFFFFFF;
	}
}
