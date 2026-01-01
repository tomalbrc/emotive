package com.cobblemonislands.emotive.newpolymer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

import java.util.Map;

public record SpaceProvider(Map<String, Float> advances) implements FontProvider {
    public static final MapCodec<SpaceProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SortedMapCodec.of(Codec.STRING, Codec.FLOAT).fieldOf("advances").forGetter(SpaceProvider::advances)
    ).apply(instance, SpaceProvider::new));

    @Override
    public MapCodec<? extends FontProvider> codec() {
        return CODEC;
    }

    public static SpaceProvider.Builder builder() {
        return new SpaceProvider.Builder();
    }


    public static class Builder implements FontProvider.Builder {
        private final Object2FloatMap<String> map = new Object2FloatOpenHashMap<>();

        private Builder() {
        }

        public SpaceProvider.Builder add(String character, int size) {
            this.map.put(character, size);
            return this;
        }

        public SpaceProvider.Builder add(int character, int size) {
            this.map.put(Character.toString(character), size);
            return this;
        }

        public SpaceProvider.Builder add(char character, int size) {
            this.map.put(Character.toString(character), size);
            return this;
        }

        public SpaceProvider build() {
            return new SpaceProvider(this.map);
        }
    }
}
