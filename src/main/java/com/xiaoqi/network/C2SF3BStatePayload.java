package com.xiaoqi.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record C2SF3BStatePayload(boolean enabled) implements CustomPayload {
	public static final CustomPayload.Id<C2SF3BStatePayload> ID = new CustomPayload.Id<>(Identifier.of("carpet-visualfix", "f3b_state"));
	public static final PacketCodec<PacketByteBuf, C2SF3BStatePayload> CODEC = CustomPayload.codecOf(
		C2SF3BStatePayload::write,
		C2SF3BStatePayload::new
	);

	public C2SF3BStatePayload(PacketByteBuf buf) {
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
