package org.hedgetech.waxeverything.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.hedgetech.waxeverything.Constants;
import org.jspecify.annotations.NonNull;

/**
 * S->C: Full wax data for a single chunk
 * Sent when a player begins tracking a chunk that has at least one waxed block.
 */
public record SyncWaxedChunkPacket(long chunkPos, long[] blockPositions) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncWaxedChunkPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "waxed_chunk_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncWaxedChunkPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NonNull SyncWaxedChunkPacket decode(RegistryFriendlyByteBuf buf) {
                    long chunkPos = buf.readLong();
                    long[] blocks = new long[buf.readVarInt()];
                    for (int i = 0; i < blocks.length; i++) blocks[i] = buf.readLong();
                    return new SyncWaxedChunkPacket(chunkPos, blocks);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, SyncWaxedChunkPacket packet) {
                    buf.writeLong(packet.chunkPos);
                    buf.writeVarInt(packet.blockPositions().length);
                    for (long l : packet.blockPositions()) buf.writeLong(l);
                }
            };

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
