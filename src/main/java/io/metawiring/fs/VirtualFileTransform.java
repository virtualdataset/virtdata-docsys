package io.metawiring.fs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class VirtualFileTransform {
    private final static Logger logger = LoggerFactory.getLogger(VirtualFileTransform.class);

    private static List<TransformerEntry> transformers = new ArrayList<>();
    static {
        transformers.add(new MarkdownFileTransformer.MarkdownTransformerEntry());
    }

    public DirectoryStream<Path> transformDirectoryStream(DirectoryStream<Path> upstream) {
        List<Path> paths = new ArrayList<>();
        for (Path path : paths) {
            for (TransformerEntry t : transformers) {
                if (t.matchesSource(path)) {
                    Path substitute = t.getSourceTargetMapper().apply(path);
                    paths.add(substitute);
                    logger.debug("substituted " + substitute + " for previous " + path);
                } else {
                    paths.add(path);
                }
            }
        }

        return new DirectoryStream<>() {
            @Override
            public Iterator<Path> iterator() {
                return paths.iterator();
            }
            @Override
            public void close() throws IOException {

            }
        };
    }

    private TransformedPath maybeTransform(Path path) {

        for (TransformerEntry t: transformers) {
            if (t.matchesSource(path)) {
                TransformedPath transformed = t.getFileTransformer().apply(path);
                return transformed;
            }
        }
        return null;
    }


    public InputStream newInputStream(Path path) {

        for (TransformerEntry transformer : transformers) {
            if (transformer.matchesTarget(path)) {
                TransformedPath transformed = transformer.getFileTransformer().apply(path);
                InputStream virtualInputStream = transformed.getVirtualInputStream();
                return virtualInputStream;
            }
        }
        return null;
   }

    public <A extends BasicFileAttributes> A maybeReadSourceAttributes(Path path, Class<A> type, LinkOption[] options) throws IOException {
        for (TransformerEntry transformer : transformers) {
            if (transformer.matchesTarget(path)) {
                Path sourceNameForTarget = FileSystems.getDefault().getPath(transformer.getTargetSourceMapper().apply(path).toString());
                A attributes = FileSystems.getDefault().provider().readAttributes(sourceNameForTarget, type, options);
                Function<Path, TransformedPath> fileTransformer = transformer.getFileTransformer();
                TransformedPath applied = fileTransformer.apply(path);
                ByteBuffer rendered = applied.getRendered();
                int remaining = rendered.remaining();
                return (A) new MetaFileAttributes(attributes, remaining);
            }
        }
        return null;
    }

    public Map<String, Object> maybeReadSourceAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        for (TransformerEntry transformer : transformers) {
            if (transformer.matchesTarget(path)) {
                Path sourceNameForTarget = FileSystems.getDefault().getPath(transformer.getTargetSourceMapper().apply(path).toString());
                return FileSystems.getDefault().provider().readAttributes(sourceNameForTarget, attributes, options);
            }
        }
        return null;
    }

    public void maybeCheckAccess(Path syspath) throws IOException {
        for (TransformerEntry transformer : transformers) {
            if (transformer.matchesTarget(syspath)) {
                Path sourceNameForTarget= FileSystems.getDefault().getPath(transformer.getTargetSourceMapper().apply(syspath).toString());
                FileSystems.getDefault().provider().checkAccess(sourceNameForTarget);
            }
        }
    }
}
