package io.metawiring.metafs.fs.render;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.Function;

public interface RenderedFileContent {

    Path getSourcePath();
    Path getRenderedPath();

    ByteBuffer getRenderedContent();

    default InputStream getVirtualInputStream() {
        ByteBuffer rendered = getRenderedContent();
        return new ByteArrayInputStream(rendered.array());
    }

    Function<ByteBuffer, ByteBuffer> getContentTransformer();

}
