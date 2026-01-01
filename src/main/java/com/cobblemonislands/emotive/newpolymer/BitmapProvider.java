package com.cobblemonislands.emotive.newpolymer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record BitmapProvider(ResourceLocation file, List<String> chars, int ascent, int height) implements FontProvider {
    public static final MapCodec<BitmapProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("file").forGetter(BitmapProvider::file),
            Codec.STRING.listOf().fieldOf("chars").forGetter(BitmapProvider::chars),
            Codec.INT.fieldOf("ascent").forGetter(BitmapProvider::ascent),
            Codec.INT.optionalFieldOf("height", 8).forGetter(BitmapProvider::height)
    ).apply(instance, BitmapProvider::new));


    public BitmapProvider(ResourceLocation file, List<String> chars, int ascent) {
        this(file, chars, ascent, 8);
    }

    @Override
    public MapCodec<? extends FontProvider> codec() {
        return CODEC;
    }

    public static BitmapProvider.Builder builder(ResourceLocation file) {
        return new BitmapProvider.Builder(file);
    }

    public static class Builder implements FontProvider.Builder {
        private final List<String> chars = new ArrayList<>();
        private final ResourceLocation file;
        private int ascent = 7;
        private int height = 8;

        private Builder(ResourceLocation file) {
            this.file = file;
        }

        public BitmapProvider.Builder chars(String string) {
            this.chars.add(string);
            return this;
        }

        public BitmapProvider.Builder height(int height) {
            this.height = height;
            return this;
        }

        public BitmapProvider.Builder ascent(int ascent) {
            this.ascent = ascent;
            return this;
        }

        public BitmapProvider build() {
            return new BitmapProvider(this.file, this.chars, this.ascent, this.height);
        }
    }
}
