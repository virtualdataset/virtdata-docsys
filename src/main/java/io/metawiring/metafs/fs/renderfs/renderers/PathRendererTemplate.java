package io.metawiring.metafs.fs.renderfs.renderers;

import io.metawiring.metafs.fs.renderfs.api.newness.TargetPathView;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * PathRenderers are cached renderers for a particular Path.
 * In order to create the renderer from a template, the file
 * data for the path is read and then fed to this function.
 *
 * In the case of a virtual template which is made from the
 * rendering of an earlier template in a processing pipeline,
 * the downstream template will have to be recomputed for
 * every template invocation unless some form of invalidation
 * is used.
 */
public interface PathRendererTemplate extends Function<TargetPathView,ByteBuffer> {
}
