package org.hedgetech.waxeverything.waxtracking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public sealed interface WaxTarget {

    TargetType getType();

    record BlockTarget(BlockPos pos) implements WaxTarget {
        public static final MapCodec<BlockTarget> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(BlockTarget::pos)
                ).apply(instance, BlockTarget::new)
        );

        @Override
        public TargetType getType() {
            return TargetType.BLOCK;
        }
    }

    record EntityTarget(UUID entityUuid, Optional<BlockPos> lastKnownPos) implements WaxTarget {
        public static final MapCodec<EntityTarget> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        UUIDUtil.CODEC.fieldOf("uuid").forGetter(EntityTarget::entityUuid),
                        BlockPos.CODEC.optionalFieldOf("last_known_pos").forGetter(EntityTarget::lastKnownPos)
                ).apply(instance, EntityTarget::new)
        );

        @Override
        public TargetType getType() {
            return TargetType.ENTITY;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EntityTarget entityTarget)) return false;
            return Objects.equals(this.entityUuid, entityTarget.entityUuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.entityUuid);
        }
    }

    Codec<WaxTarget> CODEC = TargetType.CODEC.dispatch(
            WaxTarget::getType,
            type -> switch(type) {
                case BLOCK -> BlockTarget.CODEC;
                case ENTITY -> EntityTarget.CODEC;
            }
    );

    enum TargetType implements StringRepresentable {
        BLOCK("block"),
        ENTITY("entity");

        public static final Codec<TargetType> CODEC = StringRepresentable.fromEnum(TargetType::values);
        private final String name;

        TargetType(String name) {
            this.name = name;
        }

        @Override
        public @NonNull String getSerializedName() {
            return name;
        }
    }
}
