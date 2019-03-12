package io.virtdata.docsys.metafs.fs.renderfs.api;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public interface FileContentRenderer {

    /**
     * @return a pattern that can be used to match path names which serve as the source data of rendered files.
     */
    Pattern getSourcePattern();

    /**
     * @return a pattern that can be used to match path names which are to be dynamically rendered from source file content.
     */
    Pattern getTargetPattern();

    default boolean matchesSource(Path p) {
        return getSourcePattern().matcher(p.toString()).matches();
    }
    default boolean matchesTarget(Path p) {
        return getTargetPattern().matcher(p.toString()).matches();
    }

    String getTargetSuffix();

    /**
     * Return the matching source path, but only if the target name matches the target extension.
     * @param targetName The target Path which represents the intended to be rendered
     * @return A source path, or null if the target name does not match for this renderer
     */
    Path getSourcePath(Path targetName);

    Path getRenderedTargetName(Path sourceName);

    default InputStream getInputStream(Path targetName) {
        ByteBuffer buf = getRendered(targetName);
        if (buf==null) {
            return null;
        }
        return new ByteArrayInputStream(buf.array());
    }

    ByteBuffer render(Path source, Path target, ByteBuffer input);

    default ByteBuffer getRendered(Path targetPath) {
        Path sourcePath = getSourcePath(targetPath);
        if (sourcePath!=null) {
            try {
                InputStream inputStream = sourcePath.getFileSystem().provider().newInputStream(sourcePath);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                inputStream.transferTo(bos);
                ByteBuffer rawInput = ByteBuffer.wrap(bos.toByteArray());
                ByteBuffer rendered = render(sourcePath, targetPath, rawInput);
                return rendered;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    default SeekableByteChannel getByteChannel(Path targetPath) {
        Path sourcePath = getSourcePath(targetPath);
        if (sourcePath!=null) {
            try {
                InputStream inputStream = sourcePath.getFileSystem().provider().newInputStream(sourcePath);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                inputStream.transferTo(bos);
                ByteBuffer rawInput = ByteBuffer.wrap(bos.toByteArray());
                ByteBuffer renderedOutput = render(sourcePath, targetPath, rawInput);
                SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();
                channel.write(renderedOutput);
                channel.position(0);
                return channel;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


}
