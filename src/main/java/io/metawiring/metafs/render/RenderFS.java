package io.metawiring.metafs.render;

import io.metawiring.metafs.virtual.VirtFS;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.LinkedList;

/**
 * The RenderFS filesystem will pretend that a rendered form
 * of some file types already exist in the filesystem, so long
 * as the necessary input file type and rendererTypes are present.
 * Directory listings, file access, and everything else that
 * would normally work for these files will work. RenderFS
 * will create in-memory versions of these files as needed.
 *
 * The rendered version of files automatically take on all
 * the attributes of their upstream file format, except
 * for file size and file data.
 */
public class RenderFS extends VirtFS {

    private final RenderFSProvider provider = RenderFSProvider.get();

    public RenderFS(FileSystem layer) {
        super(layer.getPath(layer.getSeparator()));
    }

    public LinkedList<FileContentRenderer> getRendererTypes() {
        return rendererTypes;
    }

    private LinkedList<FileContentRenderer> rendererTypes = new LinkedList<>();

    public RenderFS(Path wrapped) {
        super(wrapped);
    }

    public RenderFS(URI baseUri) {
        this(Path.of(baseUri));
    }

    public void addRenderer(FileContentRenderer rendererType) {
        this.rendererTypes.add(rendererType);
    }

    @Override
    public RenderFSProvider provider() {
        return provider;
    }


}
