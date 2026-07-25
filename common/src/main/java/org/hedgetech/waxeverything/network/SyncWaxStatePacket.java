package org.hedgetech.waxeverything.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.hedgetech.waxeverything.Constants;
import org.hedgetech.waxeverything.waxtracking.WaxTarget;
import org.jspecify.annotations.NonNull;

/**
 * S->C: A single block being waxed or unwaxed
 * Sent to all players in the dimension on every wax/unwax event.
 */
public record  SyncWaxStatePacket(WaxTarget target, boolean waxed) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncWaxStatePacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "wax_state_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncWaxStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecTrusted(WaxTarget.CODEC),
            SyncWaxStatePacket::target,
            ByteBufCodecs.BOOL,
            SyncWaxStatePacket::waxed,
            SyncWaxStatePacket::new
    );

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
