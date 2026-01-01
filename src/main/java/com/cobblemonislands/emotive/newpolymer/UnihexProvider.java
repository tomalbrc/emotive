package com.cobblemonislands.emotive.newpolymer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.List;

public record UnihexProvider(ResourceLocation hexFile, List<SizeOverride> sizeOverrides) implements FontProvider {
    public static final MapCodec<UnihexProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("hex_file").forGetter(UnihexProvider::hexFile),
            SizeOverride.CODEC.listOf().optionalFieldOf("size_overrides", List.of()).forGetter(UnihexProvider::sizeOverrides)
    ).apply(instance, UnihexProvider::new));

    public UnihexProvider(ResourceLocation hexFile) {
        this(hexFile, List.of());
    }

    @Override
    public MapCodec<? extends FontProvider> codec() {
        return CODEC;
    }

    public record SizeOverride(int from, int to, int left, int right) {
        public static final Codec<SizeOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.CODEPOINT.fieldOf("from").forGetter(SizeOverride::from),
                ExtraCodecs.CODEPOINT.fieldOf("to").forGetter(SizeOverride::to),
                Codec.INT.fieldOf("left").forGetter(SizeOverride::left),
                Codec.INT.fieldOf("right").forGetter(SizeOverride::right)
        ).apply(instance, SizeOverride::new));


        public SizeOverride(String from, String to, int left, int right) {
            this(from.codePointAt(0), to.codePointAt(0), left, right);
        }
    }
}
