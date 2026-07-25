package org.hedgetech.waxeverything.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.hedgetech.waxeverything.Constants;
import org.hedgetech.waxeverything.waxtracking.WaxTarget;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * S->C: Full wax data for a single chunk
 * Sent when a player begins tracking a chunk that has at least one waxed block.
 */
public record SyncWaxedChunkPacket(long chunkPos, List<WaxTarget> targets) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncWaxedChunkPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "waxed_chunk_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncWaxedChunkPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            SyncWaxedChunkPacket::chunkPos,
            ByteBufCodecs.fromCodecTrusted(WaxTarget.CODEC).apply(ByteBufCodecs.list()),
            SyncWaxedChunkPacket::targets,
            SyncWaxedChunkPacket::new
    );

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
