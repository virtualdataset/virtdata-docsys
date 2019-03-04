package io.metawiring.metafs.fs.render;

import io.metawiring.metafs.fs.virtual.VirtFS;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

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
    private LinkedList<FileContentRenderer> rendererTypes = new LinkedList<>();
    private String[] targetExtensions;
    private FileContentRenderer[] targetRenderers;


    public RenderFS(FileSystem layer) {
        super(layer.getPath(layer.getSeparator()));
    }

    public RenderFS(Path wrapped) {
        super(wrapped);
    }

    public RenderFS(URI baseUri) {
        this(Path.of(baseUri));
    }

    public LinkedList<FileContentRenderer> getRendererTypes() {
        return rendererTypes;
    }

    public void addRenderer(FileContentRenderer rendererType) {
        this.rendererTypes.add(rendererType);
        targetExtensions = rendererTypes.stream().map(FileContentRenderer::getTargetSuffix).collect(Collectors.toList()).toArray(new String[0]);
        targetRenderers = rendererTypes.toArray(new FileContentRenderer[0]);
    }

    @Override
    public RenderFSProvider provider() {
        return provider;
    }


    public DirectoryStream<Path> newDirectoryStream(DirectoryStream<Path> paths) {
        return new RenderFSDirectoryStream(paths, rendererTypes);
    }


    public BasicFileAttributes readAttributes(Path path, Class type, LinkOption... options) throws IOException {
        BasicFileAttributes attrs = null;
        try {
            attrs = super.readAttributes(path, type, options);
        } catch (Exception e1) {
            for (int i = 0; i < targetRenderers.length; i++) {
                if (path.toString().endsWith(targetExtensions[i])) {
                    FileContentRenderer renderer = targetRenderers[i];
                    Path sourcePath = renderer.getSourcePath(path);
                    try {
                        attrs = super.readAttributes(sourcePath, type, options);
                        ByteBuffer rendered = renderer.getRendered(path);
                        return new RenderedFileAttributes(attrs, rendered.remaining());
                    } catch (Exception ignored) {
                    }
                }
            }
            throw e1;
            // The first exception is the real exception
            // We were unable to find a suitable transform to the target type
        }
        return attrs;
    }

    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption[] options) throws IOException {
        throw new UnsupportedOperationException("not available in this implementation");
//        Map<String, Object> attrs = null;
//        try {
//            attrs = super.readAttributes(path, attributes, options);
//        } catch (Exception e1) {
//            for (int i = 0; i < targetRenderers.length; i++) {
//                if (path.toString().endsWith(targetExtensions[i])) {
//                    FileContentRenderer renderer = targetRenderers[i];
//                    Path sourcePath = renderer.getSourcePath(path);
//                    try {
//                        return super.readAttributes(sourcePath, attributes, options);
//                    } catch (Exception e2) {
//                        throw e1;
//                        // The first exception is the real exception
//                        // We were unable to read through the rendered image to the source file
//                    }
//                }
//            }
//            throw e1;
//            // The first exception is the real exception
//            // We were unable to find a suitable transform to the target type
//        }
//        return attrs;
    }

    public FileAttributeView getFileAttributeView(Path path, Class type, LinkOption[] options) {
        FileAttributeView view = null;
        try {
            view = super.getFileAttributeView(path, type, options);
        } catch (Exception e1) {
            for (int i = 0; i < targetRenderers.length; i++) {
                if (path.toString().endsWith(targetExtensions[i])) {
                    FileContentRenderer renderer = targetRenderers[i];
                    Path sourcePath = renderer.getSourcePath(path);
                    try {
                        view = super.getFileAttributeView(sourcePath, type, options);
                        return view;
                    } catch (Exception e2) {
//                        throw e1;
                    }
                }
            }
            throw e1;
        }
        return view;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return "RenderFS:" + this.rendererTypes.stream().map(String::valueOf).collect(Collectors.joining(",","[","]"));
    }
}