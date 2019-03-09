package io.metawiring.metafs.fs.renderfs;

import io.metawiring.metafs.core.AugmentingIterator;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

class RenderFSDirectoryStream implements DirectoryStream<Path> {

    private final DirectoryStream<Path> wrappedStream;
    private final NameMappingFunc func;

    public RenderFSDirectoryStream(DirectoryStream<Path> wrappedStream, List<FileContentRenderer> renderers) {
        this.wrappedStream = wrappedStream;
        this.func = new NameMappingFunc(renderers);
    }

    @Override
    public Iterator<Path> iterator() {
        return new AugmentingIterator<>(wrappedStream.iterator(),func);
    }

    @Override
    public void close() throws IOException {
        wrappedStream.close();
    }

    private class NameMappingFunc implements Function<Path,List<Path>> {
        private List<FileContentRenderer> renderers;

        public NameMappingFunc(List<FileContentRenderer> renderers) {
            this.renderers = renderers;
        }

        @Override
        public List<Path> apply(Path path) {
            List<Path> newpaths=null;

            for (FileContentRenderer renderer : renderers) {
                if (renderer.matchesSource(path)) {
                    if (newpaths==null) {
                        newpaths=new ArrayList<>();
                    }
                    Path renderedTargetName = renderer.getRenderedTargetName(path);
                    newpaths.add(renderedTargetName);
                }
            }
            return newpaths;
        }
    }
}
