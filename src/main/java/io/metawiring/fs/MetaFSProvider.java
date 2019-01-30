package io.metawiring.fs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MetaFSProvider extends FileSystemProvider {
    private final static Logger logger = LoggerFactory.getLogger(MetaFSProvider.class);
    FileSystem sysfs = FileSystems.getDefault();
    private Map<URI, MetaFS> filesystems = new ConcurrentHashMap<>();
    private VirtualFileTransform vtransform = new VirtualFileTransform();

    private static Path syspath(Path path) {
        if (path instanceof MetaPath) {
            MetaPath metaPath = (MetaPath) path;
            Path sysPath = metaPath.getSysPath();
            return sysPath;
        } else {
            throw new InvalidParameterException("Non-meta path was given to meta fs to convert to a system path.");
        }
    }

    @Override
    public String getScheme() {
        return "meta";
    }

    @Override
    public synchronized FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        MetaFS metaFS = new MetaFS(this, uri, env);
        logger.debug("started new meta fs on " + metaFS.getSysPath());
        filesystems.put(uri, metaFS);
        return metaFS;
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        Path syspath = syspath(path);
        try {
            InputStream inputStream = super.newInputStream(syspath);
            return inputStream;
        } catch (Exception e) {
            try {
                InputStream vinputStream = vtransform.newInputStream(path);
                return vinputStream;
            } catch (Exception e2) {
                throw new IOException("Unable to find stream for path " + path, e);
            }
        }

    }

    @Override
    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        FileChannel channel = null;
        try {
            channel = super.newFileChannel(path, options, attrs);
            return channel;
        } catch (Exception ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return filesystems.get(uri);
    }

    @Override
    public Path getPath(URI uri) {
        MetaFS extant = null;
        int matching = 0;

        if (uri.getScheme().equals(this.getScheme())) {
            throw new IllegalArgumentException("The scheme for " + uri + " does not match the scheme for " + this);
        }
        for (URI rooturi : filesystems.keySet()) {
            URI relUri = rooturi.relativize(uri);
            if (relUri.equals(uri)) {
                matching++;
                extant = filesystems.get(uri);
            }
            if (matching > 1) {
                throw new RuntimeException("Of the known filesystem instances, more than one matched " + uri);
            }
            if (matching == 0) {
                throw new FileSystemNotFoundException("Unable to match uri against any filesystem instances: " + uri);
            }
        }

        return extant.getPath(uri.getPath());
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        Path syspath = syspath(path);
        return FileSystems.getDefault().provider().newByteChannel(syspath, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        Path syspath = syspath(dir);
        DirectoryStream<Path> sysDirectoryStream = FileSystems.getDefault().provider().newDirectoryStream(syspath, filter);
        return vtransform.transformDirectoryStream(sysDirectoryStream);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        Path syspath = syspath(dir);
        FileSystems.getDefault().provider().createDirectory(syspath, attrs);
    }

    @Override
    public void delete(Path path) throws IOException {
        Path syspath = syspath(path);
        FileSystems.getDefault().provider().delete(syspath);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        Path syspathSource = syspath(source);
        Path syspathTarget = syspath(target);
        FileSystems.getDefault().provider().copy(syspathSource, syspathTarget, options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        Path syspathSource = syspath(source);
        Path syspathTarget = syspath(target);
        FileSystems.getDefault().provider().move(syspathSource, syspathTarget, options);
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        Path syspath1 = syspath(path);
        Path syspath2 = syspath(path2);
        return FileSystems.getDefault().provider().isSameFile(syspath1, syspath2);
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        Path syspath = syspath(path);
        return FileSystems.getDefault().provider().isHidden(syspath);
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return FileSystems.getDefault().provider().getFileStore(syspath(path));
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        try {
            FileSystems.getDefault().provider().checkAccess(syspath(path), modes);
        } catch (Exception e) {
            try {
                vtransform.maybeCheckAccess(syspath(path));
            } catch (Exception e2) {
                throw e;
            }
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return FileSystems.getDefault().provider().getFileAttributeView(syspath(path), type, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        Path syspath = syspath(path);
        FileSystemProvider provider = FileSystems.getDefault().provider();
        try {
            A attributes = provider.readAttributes(syspath, type, options);
            return attributes;
        } catch (Exception e) {
            try {
                A attributes = vtransform.maybeReadSourceAttributes(syspath, type, options);
                return attributes;
            } catch (Exception e2) {
                throw new IOException("Unable to read attributes for " + path);
            }
        }
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        Path syspath = syspath(path);
        FileSystemProvider provider = FileSystems.getDefault().provider();
        try {
            Map<String, Object> map = provider.readAttributes(syspath, attributes, options);
            return map;
        } catch (Exception e) {
            try {
                Map<String, Object> map = vtransform.maybeReadSourceAttributes(syspath, attributes, options);
                return map;
            } catch (Exception e2) {
                throw new IOException("Unable to read attribute map for " + path + " or " + syspath);
            }
        }
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        FileSystems.getDefault().provider().setAttribute(syspath(path), attribute, value, options);
    }

    public void unregister(MetaFS metaFS) {
        filesystems.remove(metaFS.getUri());
    }
}
