package com.cobblemonislands.emotive.newpolymer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;

public interface FontProvider {
    MapCodec<FontProvider> CODEC = MapCodec.assumeMapUnsafe(Codec.lazyInitialized(() -> FontProvider.TYPES.codec(Codec.STRING).dispatch(FontProvider::codec, Function.identity())));
    LateBoundIdMapper<String, MapCodec<? extends FontProvider>> TYPES = new LazyIdMapper<>(m -> {
        m.put("bitmap", BitmapProvider.CODEC);
        m.put("space", SpaceProvider.CODEC);
        m.put("ttf", TTFProvider.CODEC);
        m.put("unihex", UnihexProvider.CODEC);
        m.put("reference", ReferenceProvider.CODEC);
    });

    MapCodec<? extends FontProvider> codec();


    interface Builder {
        FontProvider build();
    }

}
