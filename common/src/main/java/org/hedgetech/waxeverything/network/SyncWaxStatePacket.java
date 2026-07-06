package org.hedgetech.waxeverything.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.hedgetech.waxeverything.Constants;
import org.jspecify.annotations.NonNull;

/**
 * S->C: A single block being waxed or unwaxed
 * Sent to all players in the dimension on every wax/unwax event.
 */
public record SyncWaxStatePacket(long blockPos, boolean waxed) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncWaxStatePacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "wax_state_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncWaxStatePacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NonNull SyncWaxStatePacket decode(RegistryFriendlyByteBuf buf) {
                    return new SyncWaxStatePacket(buf.readLong(), buf.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, SyncWaxStatePacket packet) {
                    buf.writeLong(packet.blockPos());
                    buf.writeBoolean(packet.waxed());
                }
            };

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
