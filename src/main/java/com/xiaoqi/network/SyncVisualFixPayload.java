package com.xiaoqi.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncVisualFixPayload(
	boolean lazyEntityRenderingFix,
	boolean tntMotionArrow,
	boolean tntNoFlashScale,
	boolean tntFuseLabel,
	boolean hideFacingArrow
) implements CustomPayload {
	public static final CustomPayload.Id<SyncVisualFixPayload> ID = new CustomPayload.Id<>(Identifier.of("carpet-visualfix", "sync"));
	public static final PacketCodec<PacketByteBuf, SyncVisualFixPayload> CODEC = CustomPayload.codecOf(
		SyncVisualFixPayload::write,
		SyncVisualFixPayload::new
	);

	public SyncVisualFixPayload(PacketByteBuf buf) {
		this(
			buf.readBoolean(),
			buf.readBoolean(),
			buf.readBoolean(),
			buf.readBoolean(),
			buf.readBoolean()
		);
	}

	public void write(PacketByteBuf buf) {
		buf.writeBoolean(this.lazyEntityRenderingFix);
		buf.writeBoolean(this.tntMotionArrow);
		buf.writeBoolean(this.tntNoFlashScale);
		buf.writeBoolean(this.tntFuseLabel);
		buf.writeBoolean(this.hideFacingArrow);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
