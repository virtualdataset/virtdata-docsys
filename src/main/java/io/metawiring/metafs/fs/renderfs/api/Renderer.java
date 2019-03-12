package io.metawiring.metafs.fs.renderfs.api;

import io.metawiring.metafs.fs.renderfs.model.TargetPathView;

import java.nio.ByteBuffer;
import java.util.function.Function;

public interface Renderer extends Function<TargetPathView, ByteBuffer> {
}
