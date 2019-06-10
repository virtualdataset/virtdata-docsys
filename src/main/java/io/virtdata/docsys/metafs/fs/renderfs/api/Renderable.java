package io.virtdata.docsys.metafs.fs.renderfs.api;

import io.virtdata.docsys.metafs.fs.renderfs.model.TargetPathView;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Renderable content
 */
public interface Renderable extends Versioned, Function<TargetPathView,ByteBuffer> {
}
