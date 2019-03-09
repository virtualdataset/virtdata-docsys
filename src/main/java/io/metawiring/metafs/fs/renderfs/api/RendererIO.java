package io.metawiring.metafs.fs.renderfs.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Function;

@SuppressWarnings("Duplicates")

public class RendererIO {

    public static String readString(Path path) {
        try {
            InputStream inputStream = path.getFileSystem().provider().newInputStream(path);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            inputStream.transferTo(bos);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static ByteBuffer readBuffer(Path path) {
        try {
            InputStream inputStream = path.getFileSystem().provider().newInputStream(path);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            inputStream.transferTo(bos);
            return ByteBuffer.wrap(bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Function<Path,ByteBuffer> PATH_BUFFER_FUNCTION = RendererIO::readBuffer;
}
