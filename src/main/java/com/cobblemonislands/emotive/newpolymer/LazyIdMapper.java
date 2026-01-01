package com.cobblemonislands.emotive.newpolymer;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import net.minecraft.util.ExtraCodecs;

public final class LazyIdMapper<A, B> extends LateBoundIdMapper<A, B> {
    private Consumer<LateBoundIdMapper<A,B>> initializer;

    public LazyIdMapper(Consumer<LateBoundIdMapper<A, B>> initializer) {
        this.initializer = initializer;
    }

    @Override
    public Codec<B> codec(Codec<A> idCodec) {
        if (this.initializer != null) {
            var init = this.initializer;
            this.initializer = null;
            init.accept(this);
        }
        return super.codec(idCodec);
    }

    @Override
    public LateBoundIdMapper<A, B> put(A id, B value) {
        if (this.initializer != null) {
            var init = this.initializer;
            this.initializer = null;
            init.accept(this);
        }
        return super.put(id, value);
    }
}
