package io.metawiring.metafs.fs.renderfs.fs;

import io.metawiring.metafs.core.MetaPath;
import io.metawiring.metafs.fs.renderfs.api.FileContentRenderer;
import io.metawiring.metafs.fs.virtual.VirtFSProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Set;

public class RenderFSProvider extends VirtFSProvider {

    protected static RenderFSProvider instance;

    public synchronized static RenderFSProvider get() {
        if (instance == null) {
            instance = new RenderFSProvider();
        }
        return instance;
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        RenderFS renderFS = assertThisFS(path);

        Path syspath = getContainerPath(path);
        try {
            InputStream inputStream = super.newInputStream(syspath);
            return inputStream;
        } catch (Exception e) {
            try {
                for (FileContentRenderer renderer : renderFS.getRendererTypes()) {
                    InputStream inputStream = renderer.getInputStream(syspath);
                    if (inputStream != null) {
                        return inputStream;
                    }
                }
            } catch (Exception e2) {
                throw new IOException("Unable to find stream for path " + path, e);
            }
            return null;
        }
    }


    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        RenderFS renderFS = assertThisFS(path);
        Path syspath = getContainerPath(path);
        try {
            SeekableByteChannel channel = syspath.getFileSystem().provider().newByteChannel(syspath, options, attrs);
            return channel;
        } catch (Exception e) {
            try {

                for (FileContentRenderer renderer : renderFS.getRendererTypes()) {
                    SeekableByteChannel channel = renderer.getByteChannel(syspath);
                    if (channel != null) {
                        return channel;
                    }
                }
            } catch (Exception e2) {
                throw new IOException("Unable to find byte channel for path " + path + ": " + e.getMessage(), e);
            }
            return null;
        }
    }

    private RenderFS assertThisFS(Path path) {
        if (!(path instanceof MetaPath)) {
            throw new InvalidParameterException("This path must be a MetaPath");
        }
        MetaPath mp = (MetaPath) path;
        if (!(mp.getFileSystem() instanceof RenderFS)) {
            throw new InvalidParameterException("This metapath must for a RenderFS");
        }

        return (RenderFS) mp.getFileSystem();
    }


    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        RenderFS renderFS = assertThisFS(dir);
        DirectoryStream<Path> paths = super.newDirectoryStream(dir, filter);
        return renderFS.newDirectoryStream(paths);
    }

    @Override
    public BasicFileAttributes readAttributes(Path path, Class type, LinkOption... options) throws IOException {
        RenderFS fs = assertThisFS(path);
        return fs.readAttributes(path, type, options);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        RenderFS renderFS = assertThisFS(path);
         return renderFS.readAttributes(path, attributes, options);
   }

    @Override
    public FileAttributeView getFileAttributeView(Path path, Class type, LinkOption... options) {
        RenderFS renderFS = assertThisFS(path);
        return renderFS.getFileAttributeView(path, type, options);
    }
}
