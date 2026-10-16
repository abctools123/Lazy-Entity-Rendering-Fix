package com.xiaoqi.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TntBatchFusePayload(
	List<TntBatchEntry> activeBatches,
	List<TntBatchEntry> lazyBatches,
	boolean hasLazyUpdate
) implements CustomPayload {
	public static final CustomPayload.Id<TntBatchFusePayload> ID = new CustomPayload.Id<>(Identifier.of("carpet-visualfix", "tnt_batch_fuse"));
	public static final PacketCodec<PacketByteBuf, TntBatchFusePayload> CODEC = CustomPayload.codecOf(
		TntBatchFusePayload::write,
		TntBatchFusePayload::new
	);

	public record TntBatchEntry(double x, double y, double z, int fuse, int count) {
		public void write(PacketByteBuf buf) {
			buf.writeDouble(this.x);
			buf.writeDouble(this.y);
			buf.writeDouble(this.z);
			buf.writeVarInt(this.fuse);
			buf.writeVarInt(this.count);
		}

		public static TntBatchEntry read(PacketByteBuf buf) {
			return new TntBatchEntry(
				buf.readDouble(),
				buf.readDouble(),
				buf.readDouble(),
				buf.readVarInt(),
				buf.readVarInt()
			);
		}
	}

	public TntBatchFusePayload(PacketByteBuf buf) {
		this(readBatches(buf), readBatches(buf), buf.readBoolean());
	}

	private static List<TntBatchEntry> readBatches(PacketByteBuf buf) {
		int size = buf.readVarInt();
		List<TntBatchEntry> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(TntBatchEntry.read(buf));
		}
		return list;
	}

	public void write(PacketByteBuf buf) {
		writeBatches(buf, this.activeBatches);
		writeBatches(buf, this.lazyBatches);
		buf.writeBoolean(this.hasLazyUpdate);
	}

	private static void writeBatches(PacketByteBuf buf, List<TntBatchEntry> list) {
		if (list == null) {
			buf.writeVarInt(0);
			return;
		}
		buf.writeVarInt(list.size());
		for (TntBatchEntry entry : list) {
			entry.write(buf);
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
