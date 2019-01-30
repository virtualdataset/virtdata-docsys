package io.metawiring.resourcetypes;

import java.nio.ByteBuffer;
import java.util.function.Function;

public interface ResourceTransformer {

    default Function<ByteBuffer, ByteBuffer> getContentTransformer() {
        return (b) -> b;
    }

    default Function<String,String> getNameTransformer() {
        return (s) -> s;
    }

    default Function<String,String> mimeTypeTransformer() {
        return (m) -> m;
    }
}
