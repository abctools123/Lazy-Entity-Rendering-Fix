package com.xiaoqi.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EntityLazyStatePayload(int entityId, boolean isLazy) implements CustomPayload {
	public static final CustomPayload.Id<EntityLazyStatePayload> ID = new CustomPayload.Id<>(Identifier.of("carpet-visualfix", "entity_lazy_state"));
	public static final PacketCodec<PacketByteBuf, EntityLazyStatePayload> CODEC = CustomPayload.codecOf(
		EntityLazyStatePayload::write,
		EntityLazyStatePayload::new
	);

	public EntityLazyStatePayload(PacketByteBuf buf) {
		this(buf.readVarInt(), buf.readBoolean());
	}

	public void write(PacketByteBuf buf) {
		buf.writeVarInt(this.entityId);
		buf.writeBoolean(this.isLazy);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
