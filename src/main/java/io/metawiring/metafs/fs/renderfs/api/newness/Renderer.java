package io.metawiring.metafs.fs.renderfs.api.newness;

import java.nio.ByteBuffer;
import java.util.function.Function;

public interface Renderer extends Function<TargetPathView, ByteBuffer> {
}
