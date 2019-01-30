package io.metawiring.fs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.Function;

public interface TransformedPath {
    Path getSourcePath();
    Path getVirtualPath();
    ByteBuffer getRendered();

    default InputStream getVirtualInputStream() {
        ByteBuffer rendered = getRendered();
        return new ByteArrayInputStream(rendered.array());
    }
    Function<ByteBuffer, ByteBuffer> getContentTransformer();

}
