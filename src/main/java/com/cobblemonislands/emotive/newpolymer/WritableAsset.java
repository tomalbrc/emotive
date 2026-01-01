package com.cobblemonislands.emotive.newpolymer;

import java.nio.charset.StandardCharsets;

public interface WritableAsset {
    byte[] toBytes();

    interface Json extends WritableAsset {
        String toJson();
        @Override
        default byte[] toBytes() {
            return toJson().getBytes(StandardCharsets.UTF_8);
        }
    }
}
