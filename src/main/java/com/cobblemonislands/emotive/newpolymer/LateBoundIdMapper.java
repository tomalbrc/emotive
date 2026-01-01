package com.cobblemonislands.emotive.newpolymer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class LateBoundIdMapper<I, V> {
    private final BiMap<I, V> idToValue = HashBiMap.create();

    public Codec<V> codec(Codec<I> codec) {
        BiMap<V, I> biMap = this.idToValue.inverse();
        BiMap var10001 = this.idToValue;
        Objects.requireNonNull(var10001);
        Function var3 = var10001::get;
        Objects.requireNonNull(biMap);
        return idResolverCodec(codec, var3, biMap::get);
    }

    public LateBoundIdMapper<I, V> put(I object, V object2) {
        Objects.requireNonNull(object2, () -> "Value for " + String.valueOf(object) + " is null");
        this.idToValue.put(object, object2);
        return this;
    }

    public Set<V> values() {
        return Collections.unmodifiableSet(this.idToValue.values());
    }

    public static <I, E> Codec<E> idResolverCodec(Codec<I> codec, Function<I, @Nullable E> function, Function<E, @Nullable I> function2) {
        return codec.flatXmap((object) -> {
            E object2 = (E) function.apply(object);
            return object2 == null ? DataResult.error(() -> "Unknown element id: " + String.valueOf(object)) : DataResult.success(object2);
        }, (object) -> {
            I object2 = (I) function2.apply(object);
            return object2 == null ? DataResult.error(() -> "Element with unknown id: " + String.valueOf(object)) : DataResult.success(object2);
        });
    }
}
