package com.xiaoqi.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncVisualFixPayload(boolean enabled) implements CustomPayload {
	public static final CustomPayload.Id<SyncVisualFixPayload> ID = new CustomPayload.Id<>(Identifier.of("carpet-visualfix", "sync"));
	public static final PacketCodec<PacketByteBuf, SyncVisualFixPayload> CODEC = CustomPayload.codecOf(
		SyncVisualFixPayload::write,
		SyncVisualFixPayload::new
	);

	public SyncVisualFixPayload(PacketByteBuf buf) {
		this(buf.readBoolean());
	}

	public void write(PacketByteBuf buf) {
		buf.writeBoolean(this.enabled);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
