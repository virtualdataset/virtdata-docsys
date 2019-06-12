package io.virtdata.docsys.metafs.fs.renderfs.api;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Renderers {

    private LinkedList<FileContentRenderer> rendererTypes = new LinkedList<>();

    public void add(FileContentRenderer renderer) {
        this.rendererTypes.add(renderer);
    }

    public List<FileContentRenderer> getRendererTypes() {
        return Collections.unmodifiableList(rendererTypes);
    }

    /**
     * Return the first renderer which matches the target extension
     * and which also has a readable source file under the source extension,
     * or null if none is available.
     * @param path The target path which is meant to be rendered
     * @return A renderer or null, if none are available
     */
    public FileContentRenderer forTargetPath(Path path) {
        for (FileContentRenderer rendererType : rendererTypes) {
            if (rendererType.canRender(path)) {
                return rendererType;
            }
        }
        return null;
    }

    public String toString() {
        return this.rendererTypes.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"));
    }
}
