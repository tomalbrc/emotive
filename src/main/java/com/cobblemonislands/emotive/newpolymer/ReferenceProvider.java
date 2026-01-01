package com.cobblemonislands.emotive.newpolymer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record ReferenceProvider(ResourceLocation id) implements FontProvider {
    public static final MapCodec<ReferenceProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(ReferenceProvider::id)
    ).apply(instance, ReferenceProvider::new));

    @Override
    public MapCodec<? extends FontProvider> codec() {
        return CODEC;
    }
}
