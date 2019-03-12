package io.virtdata.docsys.metafs.fs.renderfs.api;

import java.nio.ByteBuffer;
import java.util.function.Function;

public interface TemplateCompiler extends Function<ByteBuffer, Renderer> {
}
