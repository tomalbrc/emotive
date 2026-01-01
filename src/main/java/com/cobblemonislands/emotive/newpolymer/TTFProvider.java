package com.cobblemonislands.emotive.newpolymer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record TTFProvider(ResourceLocation file, float oversample, float size, Shift shift,
                          List<String> skip) implements FontProvider {
    public static final MapCodec<TTFProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("file").forGetter(TTFProvider::file),
            Codec.FLOAT.optionalFieldOf("oversample", 1f).forGetter(TTFProvider::oversample),
            Codec.FLOAT.optionalFieldOf("size", 11f).forGetter(TTFProvider::size),
            Shift.CODEC.optionalFieldOf("shift", Shift.NONE).forGetter(TTFProvider::shift),
            compactListCodec(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("skip", List.of("")).forGetter(TTFProvider::skip)
    ).apply(instance, TTFProvider::new));

    public TTFProvider(ResourceLocation file) {
        this(file, 1, 11, Shift.NONE, List.of());
    }

    @Override
    public MapCodec<? extends FontProvider> codec() {
        return CODEC;
    }

    public record Shift(float x, float y) {
        public static final Shift NONE = new Shift(0.0F, 0.0F);
        public static final Codec<Shift> CODEC = Codec.floatRange(-512.0F, 512.0F).listOf().comapFlatMap((floatList) -> {
            return Util.fixedSize(floatList, 2).map((floatListx) -> {
                return new Shift(floatListx.get(0), floatListx.get(1));
            });
        }, (shift) -> {
            return List.of(shift.x, shift.y);
        });
    }

    public static <E> Codec<List<E>> compactListCodec(Codec<E> codec, Codec<List<E>> codec2) {
        return Codec.either(codec2, codec).xmap((either) -> either.map((list) -> list, List::of), (list) -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list));
    }
}
